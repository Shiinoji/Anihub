package com.watchlist.anihub.data.local

/**
 * Represents the various tracking statuses for an anime in the user's watchlist.
 * These are mapped to MyAnimeList's standard tracking categories.
 */
enum class WatchlistStatus {
    /** The user intends to watch this anime in the future. */
    PLAN_TO_WATCH, 
    
    /** The user is currently watching this anime. */
    WATCHING, 
    
    /** The user has completed watching all episodes of this anime. */
    FINISHED, 
    
    /** The user has paused watching this anime. */
    ON_HOLD, 
    
    /** The user has stopped watching this anime before finishing. */
    DROPPED;

    /**
     * Returns a user-friendly string for the status.
     */
    fun getDisplayName(): String = when (this) {
        PLAN_TO_WATCH -> "Plan to Watch"
        WATCHING -> "Watching"
        FINISHED -> "Finished"
        ON_HOLD -> "On-Hold"
        DROPPED -> "Dropped"
    }
}
