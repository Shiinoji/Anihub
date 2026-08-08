package com.watchlist.anihub.ui.screens.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.MalXmlParser
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.AnimeEntity
import com.watchlist.anihub.data.local.WatchlistStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * ViewModel responsible for data-heavy operations like cache management,
 * and importing/exporting the watchlist from/to MyAnimeList (MAL) XML files.
 */
@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val animeDao: AnimeDao,
    application: Application
) : AndroidViewModel(application) {

    private val _cacheSize = MutableStateFlow("0 B")
    /**
     * Observable string representing the current size of the app's cache.
     */
    val cacheSize = _cacheSize.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    /**
     * Current state of the MAL XML import process (Idle, Loading with progress, Success, or Error).
     */
    val importState = _importState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    init {
        updateCacheSize()
    }

    /**
     * Calculates the total size of the app's cache directory on a background thread.
     */
    fun updateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val size = getCacheSize(getApplication<Application>().cacheDir)
            _cacheSize.value = formatFileSize(size)
        }
    }

    private fun getCacheSize(dir: File?): Long {
        var size: Long = 0
        dir?.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getCacheSize(file) else file.length()
        }
        return size
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    /**
     * Deletes all files within the app's internal cache directory.
     */
    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val cacheDir = getApplication<Application>().cacheDir
            cacheDir.listFiles()?.forEach { deleteDir(it) }
            updateCacheSize()
            _snackbarMessage.value = "Cache cleared successfully."
        }
    }

    private fun deleteDir(file: File?): Boolean {
        return if (file != null && file.isDirectory) {
            val children = file.list()
            children?.forEach { child ->
                deleteDir(File(file, child))
            }
            file.delete()
        } else file?.delete() ?: false
    }

    /**
     * Imports a MyAnimeList XML file selected by the user.
     * Maps MAL statuses to app-specific [WatchlistStatus] and skips duplicates.
     *
     * @param uri The URI of the XML file provided by the system document picker.
     */
    fun importMalList(uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading(0f)
            try {
                val result = withContext(Dispatchers.IO) {
                    val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?: throw Exception("Could not open file")
                    val parser = MalXmlParser()
                    val malAnimeList = parser.parse(inputStream)
                    
                    var imported = 0
                    var skipped = 0
                    var failed = 0
                    
                    malAnimeList.forEachIndexed { index, malAnime ->
                        try {
                            if (animeDao.isAnimeInWatchlist(malAnime.id)) {
                                skipped++
                            } else {
                                animeDao.insertAnime(
                                    AnimeEntity(
                                        id = malAnime.id,
                                        title = malAnime.title,
                                        imageUrl = "", // Images are fetched later from remote API
                                        status = malAnime.status,
                                        userScore = malAnime.score,
                                        episodesWatched = malAnime.watchedEpisodes,
                                        startDate = malAnime.startDate,
                                        finishDate = malAnime.finishDate
                                    )
                                )
                                imported++
                            }
                        } catch (e: Exception) {
                            failed++
                        }
                        // Update progress percentage
                        _importState.value = ImportState.Loading((index + 1).toFloat() / malAnimeList.size)
                    }
                    ImportResult(imported, skipped, failed)
                }
                _importState.value = ImportState.Success(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Exports the current local watchlist to a MAL-compatible XML file.
     *
     * @param uri The URI where the file should be saved (provided by [ActivityResultContracts.CreateDocument]).
     */
    fun exportWatchlist(uri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val watchlist = animeDao.getWatchlist().first()
                    val xml = buildXml(watchlist)
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(xml.toByteArray())
                    }
                }
                _snackbarMessage.value = "Watchlist exported successfully."
            } catch (e: Exception) {
                _snackbarMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    private fun buildXml(watchlist: List<AnimeEntity>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<myanimelist>\n")
        watchlist.forEach { anime ->
            sb.append("  <anime>\n")
            sb.append("    <series_animedb_id>${anime.id}</series_animedb_id>\n")
            sb.append("    <series_title><![CDATA[${anime.title}]]></series_title>\n")
            sb.append("    <my_status>${mapStatusToMal(anime.status)}</my_status>\n")
            sb.append("    <my_score>${anime.userScore ?: 0}</my_score>\n")
            sb.append("    <my_watched_episodes>${anime.episodesWatched ?: 0}</my_watched_episodes>\n")
            sb.append("    <my_start_date>${anime.startDate ?: "0000-00-00"}</my_start_date>\n")
            sb.append("    <my_finish_date>${anime.finishDate ?: "0000-00-00"}</my_finish_date>\n")
            sb.append("  </anime>\n")
        }
        sb.append("</myanimelist>")
        return sb.toString()
    }

    private fun mapStatusToMal(status: WatchlistStatus): String {
        return when (status) {
            WatchlistStatus.WATCHING -> "Watching"
            WatchlistStatus.FINISHED -> "Completed"
            WatchlistStatus.ON_HOLD -> "On-Hold"
            WatchlistStatus.DROPPED -> "Dropped"
            WatchlistStatus.PLAN_TO_WATCH -> "Plan to Watch"
        }
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }

    fun onSnackbarShown() {
        _snackbarMessage.value = null
    }

    /**
     * UI states for the import process.
     */
    sealed class ImportState {
        object Idle : ImportState()
        data class Loading(val progress: Float) : ImportState()
        data class Success(val result: ImportResult) : ImportState()
        data class Error(val message: String) : ImportState()
    }

    /**
     * Summary of an import operation.
     */
    data class ImportResult(val imported: Int, val skipped: Int, val failed: Int)
}
