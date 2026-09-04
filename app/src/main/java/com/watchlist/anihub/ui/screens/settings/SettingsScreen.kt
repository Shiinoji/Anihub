package com.watchlist.anihub.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    val themeMode by viewModel.themeMode.collectAsState()
    val colorPalette by viewModel.colorPalette.collectAsState()
    val titleLanguage by viewModel.titleLanguage.collectAsState()
    val staffLanguage by viewModel.staffLanguage.collectAsState()
    val scoreFormat by viewModel.scoreFormat.collectAsState()
    val airingFormat by viewModel.airingFormat.collectAsState()
    val adultContent by viewModel.adultContent.collectAsState()
    val showAiringCountdown by viewModel.showAiringCountdown.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val dynamicTheme by viewModel.dynamicTheme.collectAsState()
    val displayScale by viewModel.displayScale.collectAsState()

    val dataViewModel: DataManagementViewModel = hiltViewModel()
    val cacheSize by dataViewModel.cacheSize.collectAsState()
    val importState by dataViewModel.importState.collectAsState()
    val snackbarMessage by dataViewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val malPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { dataViewModel.importMalList(it) } }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/xml")
    ) { uri: Uri? -> uri?.let { dataViewModel.exportWatchlist(it) } }

    var showTitleDialog by remember { mutableStateOf(false) }
    var showStaffDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var showAiringDialog by remember { mutableStateOf(false) }
    var showScaleDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

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
            labelProvider = { lang -> lang.name.lowercase().replaceFirstChar { it.uppercase() } }
        )
    }

    if (showStaffDialog) {
        EnumSelectionDialog(
            title = "Staff Name Language",
            options = StaffNameLanguage.entries,
            selected = staffLanguage,
            onSelect = { viewModel.setStaffLanguage(it) },
            onDismiss = { showStaffDialog = false },
            labelProvider = { staff -> staff.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() } }
        )
    }

    if (showScoreDialog) {
        EnumSelectionDialog(
            title = "Score Format",
            options = ScoreFormat.entries,
            selected = scoreFormat,
            onSelect = { viewModel.setScoreFormat(it) },
            onDismiss = { showScoreDialog = false },
            labelProvider = { score -> score.name.replace("POINT_", "Point ").replace("_", ".").lowercase().replaceFirstChar { it.uppercase() } }
        )
    }

    if (showAiringDialog) {
        EnumSelectionDialog(
            title = "Airing Format",
            options = AiringFormat.entries,
            selected = airingFormat,
            onSelect = { viewModel.setAiringFormat(it) },
            onDismiss = { showAiringDialog = false },
            labelProvider = { airing -> airing.name.lowercase().replaceFirstChar { it.uppercase() } }
        )
    }

    if (showScaleDialog) {
        EnumSelectionDialog(
            title = "Display Scale",
            options = listOf(0.8f, 0.9f, 1.0f, 1.1f, 1.2f),
            selected = displayScale,
            onSelect = { viewModel.setDisplayScale(it) },
            onDismiss = { showScaleDialog = false },
            labelProvider = { "${(it * 100).toInt()}%" }
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
                title = { 
                    Text(
                        "Settings", 
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-1).sp
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(ImageVector.vectorResource(R.drawable.arrow_left), contentDescription = "Back", modifier = Modifier.size(24.dp))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: PERSONALIZATION (PREMIUM BENTO STYLE)
            SettingsCard(title = "Personalization", icon = Icons.Default.Palette) {
                SettingsSubHeader(title = "Theme Mode")
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val themeOptions = listOf(
                        Triple("System", ThemeMode.SYSTEM, Icons.Default.SettingsSuggest),
                        Triple("Light", ThemeMode.LIGHT, Icons.Default.LightMode),
                        Triple("Dark", ThemeMode.DARK, Icons.Default.DarkMode),
                        Triple("OLED", ThemeMode.AMOLED, Icons.Default.BrightnessLow)
                    )
                    
                    themeOptions.forEach { (label, mode, icon) ->
                        Box(modifier = Modifier.padding(bottom = 8.dp)) {
                            ThemePreviewCard(
                                label = label,
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                surfaceColor = when(mode) {
                                    ThemeMode.LIGHT -> Color.White
                                    ThemeMode.DARK -> Color(0xFF1A1C1E)
                                    ThemeMode.AMOLED -> Color.Black
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                accentColor = MaterialTheme.colorScheme.primary,
                                icon = icon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSubHeader(title = "Color Accent")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PalettePreviewCard(
                        selected = colorPalette == ColorPalette.DYNAMIC,
                        onClick = { viewModel.setColorPalette(ColorPalette.DYNAMIC) },
                        accentColor = AniListBlue,
                        label = "Dynamic",
                        isDynamic = true,
                        icon = ImageVector.vectorResource(R.drawable.palette)
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: PREFERENCES
            SettingsCard(title = "Preferences", icon = Icons.Default.Tune) {
                SettingsSubHeader(title = "Localization")
                SettingsRow(
                    title = "Title Language", 
                    value = titleLanguage.name.lowercase().replaceFirstChar { it.uppercase() }, 
                    onClick = { showTitleDialog = true },
                    icon = ImageVector.vectorResource(R.drawable.languages)
                )
                SettingsRow(
                    title = "Staff Names", 
                    value = staffLanguage.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, 
                    onClick = { showStaffDialog = true },
                    icon = Icons.Default.PersonSearch
                )

                Spacer(modifier = Modifier.height(16.dp))
                SettingsSubHeader(title = "Display")
                SettingsRow(
                    title = "Score Format", 
                    value = scoreFormat.name.replace("POINT_", "Point ").replace("_", ".").lowercase().replaceFirstChar { it.uppercase() }, 
                    onClick = { showScoreDialog = true },
                    icon = ImageVector.vectorResource(R.drawable.star)
                )
                SettingsRow(
                    title = "Airing Format", 
                    value = airingFormat.name.lowercase().replaceFirstChar { it.uppercase() }, 
                    onClick = { showAiringDialog = true },
                    icon = ImageVector.vectorResource(R.drawable.calendar)
                )
                SettingsRow(
                    title = "Display Scale",
                    value = "${(displayScale * 100).toInt()}%",
                    onClick = { showScaleDialog = true },
                    icon = ImageVector.vectorResource(R.drawable.scaling)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSubHeader(title = "View History")
                SettingsRow(
                    title = "Recent Activity",
                    description = "View your watch history",
                    onClick = onHistoryClick,
                    icon = ImageVector.vectorResource(R.drawable.history)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION: ADVANCED & DATA
            SettingsCard(title = "Advanced", icon = Icons.Default.AutoMode) {
                SettingsSwitchRow(
                    title = "Background Sync",
                    description = "Enable high-frequency notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    icon = Icons.Default.Sync
                )
                
                SettingsSwitchRow(
                    title = "Airing Countdown", 
                    description = "Show live time until next episode",
                    checked = showAiringCountdown, 
                    onCheckedChange = { viewModel.setShowAiringCountdown(it) },
                    icon = ImageVector.vectorResource(R.drawable.moon)
                )

                SettingsSwitchRow(
                    title = "Adult Content (R18+)", 
                    checked = adultContent, 
                    onCheckedChange = { viewModel.setAdultContent(it) },
                    icon = Icons.Default.LockOpen
                )

                SettingsSwitchRow(
                    title = "Dynamic Theme",
                    checked = dynamicTheme,
                    onCheckedChange = { viewModel.setDynamicTheme(it) },
                    icon = Icons.Default.Palette
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsSubHeader(title = "Data Management")
                SettingsRow(title = "Import from MAL", onClick = { malPickerLauncher.launch("text/xml") }, icon = Icons.Default.FileUpload)
                SettingsRow(title = "Export Watchlist", onClick = { exportLauncher.launch("anihub_export.xml") }, icon = Icons.Default.FileDownload)
                SettingsRow(title = "Clear Cache", value = cacheSize, onClick = { showClearCacheDialog = true }, icon = Icons.Default.DeleteSweep)
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
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp
                )
            }
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
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, spring(dampingRatio = 0.7f))

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().scale(scale),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (value != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = value, 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PalettePreviewCard(
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    label: String,
    isDynamic: Boolean = false,
    icon: ImageVector? = null
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
                .background(if (isDynamic) Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta, Color.Red)) else Brush.linearGradient(listOf(accentColor, accentColor)))
                .background(if (isDynamic) Color.Transparent else accentColor)
                .clickable { onClick() }
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(24.dp),
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
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
            // Removed unused context val
            val shiinojiResId = R.drawable.shiinoji // Assuming R.drawable.shiinoji exists based on context


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
            Text("Shiinoji", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
            Text("Lead Developer", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            
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
                Text("AniList API", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
                Text("AniHub v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
fun SettingsSubHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
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
