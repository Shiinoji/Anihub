package com.watchlist.anihub

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
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
        
        val startAnimeId = intent.getIntExtra("animeId", -1)
        val startScreen = if (startAnimeId != -1) Screen.AnimeDetail(startAnimeId) else Screen.Home

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
                MainContent(themeViewModel, startScreen)
            }
        }
    }
}

@Composable
fun MainContent(themeViewModel: ThemeViewModel, startScreen: Screen = Screen.Home) {
    var currentScreen by remember { mutableStateOf(startScreen) }
    val backStack = remember { mutableStateListOf(startScreen) }

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
    
    // If startScreen is not Home, ensure Home is at the bottom of the stack
    LaunchedEffect(startScreen) {
        if (startScreen != Screen.Home) {
            if (backStack.firstOrNull() != Screen.Home) {
                backStack.add(0, Screen.Home)
            }
            if (currentScreen != startScreen) {
                navigateTo(startScreen)
            }
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
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val from = initialState
                    val to = targetState
                    
                    val fromIndex = when(from) {
                        Screen.Home -> 0
                        Screen.Search -> 1
                        Screen.Watchlist -> 2
                        else -> -1
                    }
                    val toIndex = when(to) {
                        Screen.Home -> 0
                        Screen.Search -> 1
                        Screen.Watchlist -> 2
                        else -> -1
                    }

                    if (fromIndex != -1 && toIndex != -1) {
                        if (toIndex > fromIndex) {
                            // Slide left (Right to Left)
                            (slideInHorizontally { it } + fadeIn())
                                .togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            // Slide right (Left to Right)
                            (slideInHorizontally { -it } + fadeIn())
                                .togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    } else {
                        // Default fade for other screens
                        fadeIn(tween(300))
                            .togetherWith(fadeOut(tween(300)))
                    }
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.Home -> HomeScreen(
                        onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) },
                        onNotificationsClick = { navigateTo(Screen.Notifications) },
                        onCalendarClick = { navigateTo(Screen.Calendar) },
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
                        onHistoryClick = { navigateTo(Screen.History) },
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
                    Screen.Calendar -> CalendarScreen(
                        onBackClick = { navigateBack() },
                        onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) }
                    )
                    is Screen.AnimeDetail -> AnimeDetailScreen(
                        animeId = screen.id,
                        onBackClick = { navigateBack() },
                        onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) },
                        onCharacterClick = { navigateTo(Screen.CharacterDetail(it)) }
                    )
                    is Screen.CharacterDetail -> CharacterDetailScreen(
                        characterId = screen.id,
                        onBackClick = { navigateBack() },
                        onAnimeClick = { navigateTo(Screen.AnimeDetail(it)) }
                    )
                }
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
        modifier = Modifier.height(62.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
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
                modifier = Modifier.offset(y = (-5).dp),
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = { 
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                alwaysShowLabel = true
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val screen: Screen,
    val icon: ImageVector
)
