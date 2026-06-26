package com.watchlist.anihub.data.remote

import com.squareup.moshi.JsonClass

import com.watchlist.anihub.ui.theme.ScoreFormat
import com.watchlist.anihub.ui.theme.TitleLanguage

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
data class MediaResponse(
    val Page: Page
)

@JsonClass(generateAdapter = true)
data class Page(
    val media: List<Media>
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
    val characters: CharacterConnection?,
    val recommendations: RecommendationConnection?
) {
    fun getFormattedScore(format: ScoreFormat): String {
        val score = averageScore ?: return "N/A"
        return when (format) {
            ScoreFormat.POINT_100 -> "$score"
            ScoreFormat.POINT_10_DECIMAL -> "${score / 10.0}"
            ScoreFormat.POINT_10 -> "${Math.round(score / 10.0)}"
            ScoreFormat.POINT_5 -> "${Math.round(score / 20.0)}"
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
    val image: CharacterImage
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
