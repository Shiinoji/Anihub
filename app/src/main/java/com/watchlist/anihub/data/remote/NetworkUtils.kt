package com.watchlist.anihub.data.remote

import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

object NetworkUtils {
    fun getErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException, is IOException -> "No internet connection. Please check your network."
            is HttpException -> {
                when (e.code()) {
                    429 -> "Too many requests. Please slow down."
                    404 -> "Requested content not found."
                    500, 502, 503 -> "Server error. AniList might be down."
                    else -> "Network error: ${e.code()}"
                }
            }
            else -> e.message ?: "Something went wrong. Please try again later."
        }
    }
}
