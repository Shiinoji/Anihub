package com.watchlist.anihub.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watchlist.anihub.BuildConfig
import com.watchlist.anihub.R
import com.watchlist.anihub.ui.ThemeViewModel
import com.watchlist.anihub.ui.theme.*

/**
 * Premium HyperOS-inspired Settings Screen.
 * Featuring floating cards, minimalist rows, and optimized background performance settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val colorPalette by viewModel.colorPalette.collectAsState()
    val titleLanguage by viewModel.titleLanguage.collectAsState()
    val staffLanguage by viewModel.staffLanguage.collectAsState()
    val scoreFormat by viewModel.scoreFormat.collectAsState()
    val airingFormat by viewModel.airingFormat.collectAsState()
    val adultContent by viewModel.adultContent.collectAsState()
    val showAiringCountdown by viewModel.showAiringCountdown.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val homeItemsPerRow by viewModel.homeItemsPerRow.collectAsState()

    val dataViewModel: DataManagementViewModel = hiltViewModel()
    val cacheSize by dataViewModel.cacheSize.collectAsState()
    val importState by dataViewModel.importState.collectAsState()
    val snackbarMessage by dataViewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    
    // Check if battery optimization is ignored
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(checkBatteryOptimization(context))
    }

    // Update battery status when returning from settings
    DisposableEffect(Unit) {
        onDispose { /* Cleanup if needed */ }
    }

    var showTitleDialog by remember { mutableStateOf(false) }
    var showStaffDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var showAiringDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    val malPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { dataViewModel.importMalList(it) } }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml")
    ) { uri: Uri? -> uri?.let { dataViewModel.exportWatchlist(it) } }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            dataViewModel.onSnackbarShown()
        }
    }

    // Dialog Rendering
    if (showTitleDialog) {
        EnumSelectionDialog(
            title = "Title Language",
            options = TitleLanguage.entries,
            selected = titleLanguage,
            onSelect = { viewModel.setTitleLanguage(it) },
            onDismiss = { showTitleDialog = false },
            labelProvider = { it.name.lowercase().replaceFirstChar { it.uppercase() } }
        )
    }

    if (showStaffDialog) {
        EnumSelectionDialog(
            title = "Staff Name Language",
            options = StaffNameLanguage.entries,
            selected = staffLanguage,
            onSelect = { viewModel.setStaffLanguage(it) },
            onDismiss = { showStaffDialog = false },
            labelProvider = { it.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() } }
        )
    }

    if (showScoreDialog) {
        EnumSelectionDialog(
            title = "Score Format",
            options = ScoreFormat.entries,
            selected = scoreFormat,
            onSelect = { viewModel.setScoreFormat(it) },
            onDismiss = { showScoreDialog = false },
            labelProvider = { it.name.replace("POINT_", "Point ").replace("_", ".").lowercase().replaceFirstChar { it.uppercase() } }
        )
    }

    if (showAiringDialog) {
        EnumSelectionDialog(
            title = "Airing Format",
            options = AiringFormat.entries,
            selected = airingFormat,
            onSelect = { viewModel.setAiringFormat(it) },
            onDismiss = { showAiringDialog = false },
            labelProvider = { it.name.lowercase().replaceFirstChar { it.uppercase() } }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = { Text("Are you sure you want to clear the app cache? This will not delete your watchlist or preferences.") },
            confirmButton = {
                TextButton(onClick = {
                    dataViewModel.clearCache()
                    showClearCacheDialog = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") }
            }
        )
    }

    ImportStatusDialogs(importState, dataViewModel)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(ImageVector.vectorResource(R.drawable.arrow_left), contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // SECTION: INTERFACE
            SettingsCard(title = "Interface") {
                SettingsSubHeader(title = "Appearance")
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemePreviewCard(
                        label = "System",
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        surfaceColor = MaterialTheme.colorScheme.surfaceVariant,
                        accentColor = AniListBlue,
                        icon = Icons.Default.RestartAlt
                    )
                    ThemePreviewCard(
                        label = "Light",
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        surfaceColor = Color.White,
                        accentColor = AniListBlue
                    )
                    ThemePreviewCard(
                        label = "Dark",
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        surfaceColor = Color(0xFF1A1C1E),
                        accentColor = AniListBlue
                    )
                    ThemePreviewCard(
                        label = "AMOLED",
                        selected = themeMode == ThemeMode.AMOLED,
                        onClick = { viewModel.setThemeMode(ThemeMode.AMOLED) },
                        surfaceColor = Color.Black,
                        accentColor = AniListBlue
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Color Palette", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PalettePreviewCard(
                        selected = colorPalette == ColorPalette.DYNAMIC,
                        onClick = { viewModel.setColorPalette(ColorPalette.DYNAMIC) },
                        accentColor = AniListBlue,
                        label = "Dynamic"
                    )
                    ColorPalette.entries.filter { it != ColorPalette.DYNAMIC }.forEach { palette ->
                        val (_, accent) = getPaletteColors(palette)
                        PalettePreviewCard(
                            selected = colorPalette == palette,
                            onClick = { viewModel.setColorPalette(palette) },
                            accentColor = accent,
                            label = palette.name.lowercase().replaceFirstChar { it.uppercase() }
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                SettingsSubHeader(title = "Layout")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Home Grid Columns", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Discover feed density", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SegmentedToggle(
                        options = listOf(2, 3),
                        selectedOption = homeItemsPerRow,
                        onOptionSelected = { viewModel.setHomeItemsPerRow(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: CONTENT & PERFORMANCE
            SettingsCard(title = "Content & Performance") {
                SettingsRow(
                    title = "View History",
                    description = "Recent activity tracking",
                    onClick = onHistoryClick,
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsSubHeader(title = "Localization")
                SettingsRow(title = "Anime Titles", value = titleLanguage.name.lowercase().replaceFirstChar { it.uppercase() }, onClick = { showTitleDialog = true })
                SettingsRow(title = "Staff Names", value = staffLanguage.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, onClick = { showStaffDialog = true })

                Spacer(modifier = Modifier.height(12.dp))
                SettingsSubHeader(title = "Display Formats")
                SettingsRow(title = "Score Format", value = scoreFormat.name.replace("POINT_", "Point ").replace("_", ".").lowercase().replaceFirstChar { it.uppercase() }, onClick = { showScoreDialog = true })
                SettingsRow(title = "Airing Format", value = airingFormat.name.lowercase().replaceFirstChar { it.uppercase() }, onClick = { showAiringDialog = true })

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsSubHeader(title = "Background Tasks")
                SettingsSwitchRow(
                    title = "Always-on Notifications",
                    description = "Enable high-frequency background sync",
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
                
                SettingsRow(
                    title = "Battery Optimization",
                    description = if (isIgnoringBatteryOptimizations) "Optimized for background" else "Recommended for notifications",
                    value = if (isIgnoringBatteryOptimizations) "Off" else "On",
                    onClick = { 
                        requestIgnoreBatteryOptimization(context)
                        // This won't update immediately as it's an external activity, 
                        // but user will see it when they return.
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                SettingsSubHeader(title = "App Features")
                SettingsSwitchRow(title = "Adult Content (R18+)", checked = adultContent, onCheckedChange = { viewModel.setAdultContent(it) })
                SettingsSwitchRow(title = "Airing Countdown", checked = showAiringCountdown, onCheckedChange = { viewModel.setShowAiringCountdown(it) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: DATA
            SettingsCard(title = "Data Management") {
                SettingsRow(title = "Import MyAnimeList", onClick = { malPickerLauncher.launch("text/xml") })
                SettingsRow(title = "Export Watchlist", onClick = { exportLauncher.launch("anihub_export.xml") })
                SettingsRow(title = "Clear Cache", value = cacheSize, onClick = { showClearCacheDialog = true })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: ABOUT
            AboutProfileCard(viewModel)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    description: String? = null,
    value: String? = null,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(dampingRatio = 0.7f))

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().scale(scale),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            } else {
                trailing?.invoke()
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun ThemePreviewCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    surfaceColor: Color,
    accentColor: Color,
    icon: ImageVector? = null
) {
    val scale by animateFloatAsState(if (selected) 1.05f else 1f, spring(dampingRatio = 0.6f))
    val elevation by animateDpAsState(if (selected) 8.dp else 0.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.height(100.dp).fillMaxWidth().scale(scale),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(width = 30.dp, height = 6.dp).clip(CircleShape).background(accentColor))
                    Box(modifier = Modifier.size(width = 20.dp, height = 4.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.4f)))
                }
                if (icon != null) {
                    Icon(icon, null, modifier = Modifier.align(Alignment.Center).size(24.dp), tint = accentColor)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PalettePreviewCard(
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    label: String
) {
    val scale by animateFloatAsState(if (selected) 1.1f else 1f, spring(dampingRatio = 0.5f))
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(scale)
                .then(
                    if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), CircleShape)
                )
                .padding(4.dp)
                .clip(CircleShape)
                .background(accentColor)
                .clickable { onClick() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun AboutProfileCard(viewModel: ThemeViewModel) {
    SettingsCard(title = "About") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            val shiinojiResId = remember(context) {
                context.resources.getIdentifier("shiinoji", "drawable", context.packageName)
            }

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (shiinojiResId != 0) {
                    Image(
                        painter = painterResource(id = shiinojiResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(imageVector = ImageVector.vectorResource(R.drawable.user), contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Shiinoji", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Lead Developer", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val context = LocalContext.current
                SocialButton(R.drawable.github_142_svgrepo_com) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Shiinoji/Anihub"))
                    context.startActivity(intent)
                }
                SocialButton(R.drawable.discord_fill_svgrepo_com) {
                    android.widget.Toast.makeText(context, "Coming soon", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(ImageVector.vectorResource(R.drawable.anilist_svgrepo_com), null, modifier = Modifier.size(24.dp), tint = Color(0xFF02A9FF))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("AniList API", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Powered by high-quality anime data", style = MaterialTheme.typography.bodySmall)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AniHub v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Build ${BuildConfig.VERSION_CODE}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = { viewModel.checkForUpdates() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update")
            }
        }
    }
}

@Composable
fun SocialButton(iconRes: Int, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
    ) {
        Icon(ImageVector.vectorResource(iconRes), null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SegmentedToggle(
    options: List<Int>,
    selectedOption: Int,
    onOptionSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onOptionSelected(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SettingsSubHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun <T> EnumSelectionDialog(
    title: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    labelProvider: (T) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option == selected,
                                onClick = { onSelect(option); onDismiss() }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == selected, onClick = { onSelect(option); onDismiss() })
                        Text(text = labelProvider(option), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ImportStatusDialogs(state: DataManagementViewModel.ImportState, dataViewModel: DataManagementViewModel) {
    when (state) {
        is DataManagementViewModel.ImportState.Loading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Importing List") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(state.progress * 100).toInt()}%")
                    }
                },
                confirmButton = {}
            )
        }
        is DataManagementViewModel.ImportState.Success -> {
            AlertDialog(
                onDismissRequest = { dataViewModel.resetImportState() },
                title = { Text("Complete") },
                text = { Text("Imported: ${state.result.imported}\nSkipped: ${state.result.skipped}") },
                confirmButton = { TextButton(onClick = { dataViewModel.resetImportState() }) { Text("OK") } }
            )
        }
        is DataManagementViewModel.ImportState.Error -> {
            AlertDialog(
                onDismissRequest = { dataViewModel.resetImportState() },
                title = { Text("Error") },
                text = { Text(state.message) },
                confirmButton = { TextButton(onClick = { dataViewModel.resetImportState() }) { Text("OK") } }
            )
        }
        else -> {}
    }
}

private fun checkBatteryOptimization(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun requestIgnoreBatteryOptimization(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    }
}

private fun getPaletteColors(palette: ColorPalette) = when(palette) {
    ColorPalette.SAKURA -> SakuraSurface to SakuraAccent
    ColorPalette.OCEAN -> OceanSurface to OceanAccent
    ColorPalette.FOREST -> ForestSurface to ForestAccent
    ColorPalette.LAVENDER -> LavenderSurface to LavenderAccent
    ColorPalette.MIDNIGHT -> MidnightSurface to MidnightAccent
    ColorPalette.SUNSET -> SunsetSurface to SunsetAccent
    ColorPalette.ARCTIC -> ArcticSurface to ArcticAccent
    ColorPalette.MATCHA -> MatchaSurface to MatchaAccent
    ColorPalette.CYBER -> CyberSurface to CyberAccent
    ColorPalette.AMBER -> AmberSurface to AmberAccent
    else -> Color.White to AniListBlue
}
