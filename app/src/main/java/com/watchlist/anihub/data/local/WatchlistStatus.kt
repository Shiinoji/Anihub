package com.watchlist.anihub.data.local

enum class WatchlistStatus {
    PLAN_TO_WATCH, WATCHING, FINISHED;

    fun getDisplayName(): String = when (this) {
        PLAN_TO_WATCH -> "Plan to Watch"
        WATCHING -> "Watching"
        FINISHED -> "Finished"
    }
}
