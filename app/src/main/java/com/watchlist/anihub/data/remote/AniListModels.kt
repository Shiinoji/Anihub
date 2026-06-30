package com.watchlist.anihub.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

import com.watchlist.anihub.ui.theme.ScoreFormat
import com.watchlist.anihub.ui.theme.TitleLanguage
import kotlin.math.roundToInt

@JsonClass(generateAdapter = true)
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class AniListResponse<T>(
    val data: T
)

@JsonClass(generateAdapter = true)
data class GenreResponse(
    @param:Json(name = "GenreCollection")
    val genreCollection: List<String>
)

@JsonClass(generateAdapter = true)
data class MediaResponse(
    @param:Json(name = "Page")
    val page: Page,
)

@JsonClass(generateAdapter = true)
data class Page(
    val media: List<Media>? = null,
    val airingSchedules: List<AiringSchedule>? = null,
)

@JsonClass(generateAdapter = true)
data class AiringSchedule(
    val id: Int,
    val episode: Int,
    val airingAt: Long,
    val media: Media,
)

@JsonClass(generateAdapter = true)
data class Media(
    val id: Int,
    val title: MediaTitle,
    val coverImage: MediaCoverImage,
    val bannerImage: String?,
    val description: String?,
    val status: String?,
    val episodes: Int?,
    val averageScore: Int?,
    val nextAiringEpisode: AiringEpisode?,
    val genres: List<String>?,
    val trailer: MediaTrailer?,
    val characters: CharacterConnection?,
    val recommendations: RecommendationConnection?,
) {
    fun getFormattedScore(format: ScoreFormat): String {
        val score = averageScore ?: return "N/A"
        return when (format) {
            ScoreFormat.POINT_100 -> score.toString()
            ScoreFormat.POINT_10_DECIMAL -> "${score / 10.0}"
            ScoreFormat.POINT_10 -> "${(score / 10.0f).roundToInt()}"
            ScoreFormat.POINT_5 -> "${(score / 20.0f).roundToInt()}"
            ScoreFormat.POINT_3 -> {
                when {
                    score >= 75 -> "3"
                    score >= 45 -> "2"
                    else -> "1"
                }
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class MediaTrailer(
    val id: String?,
    val site: String?,
    val thumbnail: String?
) {
    val url: String? get() = when (site) {
        "youtube" -> "https://www.youtube.com/watch?v=$id"
        "dailymotion" -> "https://www.dailymotion.com/video/$id"
        else -> null
    }
}

@JsonClass(generateAdapter = true)
data class AiringEpisode(
    val airingAt: Long,
    val timeUntilAiring: Int,
    val episode: Int
)

@JsonClass(generateAdapter = true)
data class MediaTitle(
    val english: String?,
    val romaji: String?,
    val native: String?
) {
    fun getDisplayTitle(language: TitleLanguage): String = when (language) {
        TitleLanguage.ENGLISH -> english ?: romaji ?: native
        TitleLanguage.ROMAJI -> romaji ?: english ?: native
        TitleLanguage.NATIVE -> native ?: romaji ?: english
    } ?: "Unknown"

    val displayTitle: String get() = english ?: romaji ?: native ?: "Unknown"
}

@JsonClass(generateAdapter = true)
data class MediaCoverImage(
    val extraLarge: String?,
    val large: String?,
    val medium: String?
)

@JsonClass(generateAdapter = true)
data class CharacterConnection(
    val nodes: List<Character>?
)

@JsonClass(generateAdapter = true)
data class Character(
    val id: Int,
    val name: CharacterName,
    val image: CharacterImage,
    val description: String?,
    val gender: String?,
    val dateOfBirth: FuzzyDate?,
    val age: String?,
    val bloodType: String?,
    val media: MediaConnection?
)

@JsonClass(generateAdapter = true)
data class FuzzyDate(
    val year: Int?,
    val month: Int?,
    val day: Int?
)

@JsonClass(generateAdapter = true)
data class MediaConnection(
    val nodes: List<Media>?
)

@JsonClass(generateAdapter = true)
data class CharacterResponse(
    @param:Json(name = "Character")
    val character: Character
)

@JsonClass(generateAdapter = true)
data class CharacterName(
    val full: String?
)

@JsonClass(generateAdapter = true)
data class CharacterImage(
    val large: String?
)

@JsonClass(generateAdapter = true)
data class RecommendationConnection(
    val nodes: List<Recommendation>?
)

@JsonClass(generateAdapter = true)
data class Recommendation(
    val mediaRecommendation: Media?
)
