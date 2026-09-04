package com.watchlist.anihub.data

import android.util.Xml
import com.watchlist.anihub.data.local.WatchlistStatus
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

/**
 * A parser for MyAnimeList (MAL) export XML files.
 * Uses [XmlPullParser] to efficiently extract anime metadata.
 */
class MalXmlParser {

    /**
     * Internal representation of an anime entry extracted from the XML.
     */
    data class MalAnime(
        val id: Int,
        val title: String,
        val status: WatchlistStatus,
        val score: Double?,
        val watchedEpisodes: Int,
        val startDate: String?,
        val finishDate: String?
    )

    /**
     * Parses an [InputStream] containing MyAnimeList XML data.
     *
     * @param inputStream The stream of the XML file.
     * @return A list of [MalAnime] objects containing the parsed data.
     * @throws Exception if the XML is malformed.
     */
    fun parse(inputStream: InputStream): List<MalAnime> {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return readMyAnimeList(parser)
    }

    private fun readMyAnimeList(parser: XmlPullParser): List<MalAnime> {
        val entries = mutableListOf<MalAnime>()

        parser.require(XmlPullParser.START_TAG, null, "myanimelist")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "anime") {
                entries.add(readAnimeEntry(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    private fun readAnimeEntry(parser: XmlPullParser): MalAnime {
        parser.require(XmlPullParser.START_TAG, null, "anime")
        var id = 0
        var title = ""
        var status = WatchlistStatus.PLAN_TO_WATCH
        var score: Double? = null
        var watchedEpisodes = 0
        var startDate: String? = null
        var finishDate: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "series_animedb_id" -> id = parser.nextText().toIntOrNull() ?: 0
                "series_title" -> title = parser.nextText()
                "my_status" -> status = mapMalStatus(parser.nextText())
                "my_score" -> score = parser.nextText().toDoubleOrNull()?.takeIf { it > 0 }
                "my_watched_episodes" -> watchedEpisodes = parser.nextText().toIntOrNull() ?: 0
                "my_start_date" -> startDate = parser.nextText().takeIf { it != "0000-00-00" }
                "my_finish_date" -> finishDate = parser.nextText().takeIf { it != "0000-00-00" }
                else -> skip(parser)
            }
        }
        return MalAnime(id, title, status, score, watchedEpisodes, startDate, finishDate)
    }

    /**
     * Skips an entire XML tag and its children.
     */
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    /**
     * Maps MyAnimeList string statuses to our internal [WatchlistStatus] enum.
     */
    private fun mapMalStatus(malStatus: String): WatchlistStatus {
        return when (malStatus.lowercase()) {
            "watching" -> WatchlistStatus.WATCHING
            "completed" -> WatchlistStatus.FINISHED
            "on-hold" -> WatchlistStatus.ON_HOLD
            "dropped" -> WatchlistStatus.DROPPED
            "plan to watch" -> WatchlistStatus.PLAN_TO_WATCH
            else -> WatchlistStatus.PLAN_TO_WATCH
        }
    }
}
