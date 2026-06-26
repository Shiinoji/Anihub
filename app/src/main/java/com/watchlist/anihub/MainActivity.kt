package com.watchlist.anihub

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.watchlist.anihub.ui.ThemeViewModel
import com.watchlist.anihub.ui.navigation.Screen
import com.watchlist.anihub.ui.screens.*
import com.watchlist.anihub.ui.theme.AnihubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            val colorPalette by themeViewModel.colorPalette.collectAsState()
            val titleLanguage by themeViewModel.titleLanguage.collectAsState()
            val scoreFormat by themeViewModel.scoreFormat.collectAsState()
            val showAiringCountdown by themeViewModel.showAiringCountdown.collectAsState()

            // Handle notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
                LaunchedEffect(Unit) {
                    if (!permissionState.status.isGranted) {
                        permissionState.launchPermissionRequest()
                    }
                }
            }

            AnihubTheme(
                themeMode = themeMode,
                colorPalette = colorPalette,
                titleLanguage = titleLanguage,
                scoreFormat = scoreFormat,
                showAiringCountdown = showAiringCountdown
            ) {
                MainContent(themeViewModel)
            }
        }
    }
}

@Composable
fun MainContent(themeViewModel: ThemeViewModel) {
    var currentScreen: Screen by remember { mutableStateOf(Screen.Home) }
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(screen)
            currentScreen = screen
        }
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            currentScreen = backStack.last()
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    Scaffold(
        bottomBar = {
            if (currentScreen !is Screen.AnimeDetail && currentScreen !is Screen.Settings && currentScreen !is Screen.History && currentScreen !is Screen.Notifications) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { navigateTo(it) }
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    )
{ padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = currentScreen) {
                Screen.Home -> HomeScreen(
                    onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) },
                    onNotificationsClick = { navigateTo(Screen.Notifications) },
                    onHistoryClick = { navigateTo(Screen.History) },
                    onSettingsClick = { navigateTo(Screen.Settings) }
                )
                Screen.Search -> SearchScreen(
                    onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) }
                )
                Screen.Watchlist -> WatchlistScreen(
                    onBackClick = { navigateBack() },
                    onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) }
                )
                Screen.Settings -> SettingsScreen(
                    onBackClick = { navigateBack() },
                    viewModel = themeViewModel
                )
                Screen.History -> HistoryScreen(
                    onBackClick = { navigateBack() },
                    onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) }
                )
                Screen.Notifications -> NotificationScreen(
                    onBackClick = { navigateBack() },
                    onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) }
                )
                is Screen.AnimeDetail -> AnimeDetailScreen(
                    animeId = screen.id,
                    onBackClick = { navigateBack() },
                    onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            BottomNavItem("Home", Screen.Home, ImageVector.vectorResource(id = R.drawable.house)),
            BottomNavItem("Search", Screen.Search, ImageVector.vectorResource(id = R.drawable.search)),
            BottomNavItem("Watchlist", Screen.Watchlist, ImageVector.vectorResource(id = R.drawable.bookmark))
        )

        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                label = { 
                    Text(
                        text = item.title,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val screen: Screen,
    val icon: ImageVector
)
