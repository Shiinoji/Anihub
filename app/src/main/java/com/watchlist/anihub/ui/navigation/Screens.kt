package com.watchlist.anihub.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data object Search : Screen
    @Serializable data object Watchlist : Screen
    @Serializable data object Settings : Screen
    @Serializable data object History : Screen
    @Serializable data object Notifications : Screen
    @Serializable data class AnimeDetail(val id: Int) : Screen
}
