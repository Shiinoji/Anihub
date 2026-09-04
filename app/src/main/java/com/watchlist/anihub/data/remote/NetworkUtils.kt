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
                    403 -> "Server error! Please try again later."
                    404 -> "Requested content not found."
                    429 -> "Too many requests. Please slow down."
                    500, 502, 503 -> "The server is currently experiencing issues. Please try again later."
                    else -> "Network error (${e.code()}). Please try again later."
                }
            }
            else -> e.message ?: "Something went wrong. Please try again later."
        }
    }
}
