package com.watchlist.anihub.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class SingleMediaResponse(
    val Media: Media
)

interface AniListService {
    @POST("/")
    suspend fun getAnimeList(@Body request: GraphQLRequest): AniListResponse<MediaResponse>

    @POST("/")
    suspend fun getAnimeDetail(@Body request: GraphQLRequest): AniListResponse<SingleMediaResponse>
}

object AniListQueries {
    const val TRENDING_NOW = """
        query (${'$'}page: Int, ${'$'}perPage: Int, ${'$'}isAdult: Boolean) {
          Page(page: ${'$'}page, perPage: ${'$'}perPage) {
            media(sort: TRENDING_DESC, type: ANIME, isAdult: ${'$'}isAdult) {
              id
              title { english romaji native }
              coverImage { extraLarge large medium }
            }
          }
        }
    """

    const val MOST_POPULAR = """
        query (${'$'}page: Int, ${'$'}perPage: Int, ${'$'}isAdult: Boolean) {
          Page(page: ${'$'}page, perPage: ${'$'}perPage) {
            media(sort: POPULARITY_DESC, type: ANIME, isAdult: ${'$'}isAdult) {
              id
              title { english romaji native }
              coverImage { extraLarge large medium }
            }
          }
        }
    """

    const val SEASONAL_ANIME = """
        query (${'$'}page: Int, ${'$'}perPage: Int, ${'$'}season: MediaSeason, ${'$'}seasonYear: Int, ${'$'}isAdult: Boolean) {
          Page(page: ${'$'}page, perPage: ${'$'}perPage) {
            media(season: ${'$'}season, seasonYear: ${'$'}seasonYear, type: ANIME, sort: POPULARITY_DESC, isAdult: ${'$'}isAdult) {
              id
              title { english romaji native }
              coverImage { extraLarge large medium }
            }
          }
        }
    """

    const val TOP_RATED = """
        query (${'$'}page: Int, ${'$'}perPage: Int, ${'$'}isAdult: Boolean) {
          Page(page: ${'$'}page, perPage: ${'$'}perPage) {
            media(sort: SCORE_DESC, type: ANIME, isAdult: ${'$'}isAdult) {
              id
              title { english romaji native }
              coverImage { extraLarge large medium }
            }
          }
        }
    """

    const val ALL_TIME_POPULAR = """
        query (${'$'}page: Int, ${'$'}perPage: Int, ${'$'}isAdult: Boolean) {
          Page(page: ${'$'}page, perPage: ${'$'}perPage) {
            media(sort: POPULARITY_DESC, type: ANIME, isAdult: ${'$'}isAdult) {
              id
              title { english romaji native }
              coverImage { extraLarge large medium }
            }
          }
        }
    """

    const val ANIME_DETAIL = """
        query (${'$'}id: Int) {
          Media(id: ${'$'}id, type: ANIME) {
            id
            title { english romaji native }
            coverImage { extraLarge large medium }
            bannerImage
            description
            status
            episodes
            averageScore
            nextAiringEpisode {
              airingAt
              timeUntilAiring
              episode
            }
            characters {
              nodes {
                id
                name { full }
                image { large }
              }
            }
            recommendations {
              nodes {
                mediaRecommendation {
                  id
                  title { english romaji native }
                  coverImage { extraLarge large medium }
                }
              }
            }
          }
        }
    """

    const val AIRING_CHECK = """
        query (${'$'}ids: [Int]) {
          Page(page: 1, perPage: 50) {
            media(id_in: ${'$'}ids, type: ANIME) {
              id
              title { english romaji native }
              status
              nextAiringEpisode {
                episode
                airingAt
              }
            }
          }
        }
    """
}
