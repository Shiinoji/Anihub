package com.watchlist.anihub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.watchlist.anihub.BuildConfig
import com.watchlist.anihub.R
import com.watchlist.anihub.ui.ThemeViewModel
import com.watchlist.anihub.ui.theme.*

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

    var showTitleDialog by remember { mutableStateOf(false) }
    var showStaffDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var showAiringDialog by remember { mutableStateOf(false) }

    if (showTitleDialog) {
        EnumSelectionDialog(
            title = "Title Language",
            options = TitleLanguage.entries,
            selected = titleLanguage,
            onSelect = { viewModel.setTitleLanguage(it) },
            onDismiss = { showTitleDialog = false },
            labelProvider = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        )
    }

    if (showStaffDialog) {
        EnumSelectionDialog(
            title = "Staff Name Language",
            options = StaffNameLanguage.entries,
            selected = staffLanguage,
            onSelect = { viewModel.setStaffLanguage(it) },
            onDismiss = { showStaffDialog = false },
            labelProvider = { it.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } }
        )
    }

    if (showScoreDialog) {
        EnumSelectionDialog(
            title = "Score Format",
            options = ScoreFormat.entries,
            selected = scoreFormat,
            onSelect = { viewModel.setScoreFormat(it) },
            onDismiss = { showScoreDialog = false },
            labelProvider = { it.name.replace("POINT_", "Point ").replace("_", ".").lowercase().replaceFirstChar { c -> c.uppercase() } }
        )
    }

    if (showAiringDialog) {
        EnumSelectionDialog(
            title = "Airing Format",
            options = AiringFormat.entries,
            selected = airingFormat,
            onSelect = { viewModel.setAiringFormat(it) },
            onDismiss = { showAiringDialog = false },
            labelProvider = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(ImageVector.vectorResource(R.drawable.arrow_left), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview Mockup
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.secondary))
                        Box(modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.tertiary))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.align(Alignment.End).size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Appearance Card
            SettingsSection(title = "Appearance") {
                Text("Theme Mode", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemeOption(
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        icon = { Icon(Icons.Default.RestartAlt, contentDescription = null) }
                    )
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    ThemeOption(
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        color = Color.White
                    )
                    ThemeOption(
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        color = Color(0xFF211F1F)
                    )
                    ThemeOption(
                        selected = themeMode == ThemeMode.AMOLED,
                        onClick = { viewModel.setThemeMode(ThemeMode.AMOLED) },
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Color Palette", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaletteOption(
                        selected = colorPalette == ColorPalette.DYNAMIC,
                        onClick = { viewModel.setColorPalette(ColorPalette.DYNAMIC) },
                        icon = { Icon(Icons.Default.Palette, contentDescription = null) }
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.BROWN,
                        onClick = { viewModel.setColorPalette(ColorPalette.BROWN) },
                        colorTop = Color.White,
                        colorBottom = BrownPrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.DEEP_BROWN,
                        onClick = { viewModel.setColorPalette(ColorPalette.DEEP_BROWN) },
                        colorTop = Color.White,
                        colorBottom = DeepBrownPrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.PURPLE,
                        onClick = { viewModel.setColorPalette(ColorPalette.PURPLE) },
                        colorTop = Color.White,
                        colorBottom = PurplePrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.DEEP_PURPLE,
                        onClick = { viewModel.setColorPalette(ColorPalette.DEEP_PURPLE) },
                        colorTop = Color.White,
                        colorBottom = DeepPurplePrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.OCEAN,
                        onClick = { viewModel.setColorPalette(ColorPalette.OCEAN) },
                        colorTop = Color.White,
                        colorBottom = OceanPrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.FOREST,
                        onClick = { viewModel.setColorPalette(ColorPalette.FOREST) },
                        colorTop = Color.White,
                        colorBottom = ForestPrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.CHERRY,
                        onClick = { viewModel.setColorPalette(ColorPalette.CHERRY) },
                        colorTop = Color.White,
                        colorBottom = CherryPrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.SUNSET,
                        onClick = { viewModel.setColorPalette(ColorPalette.SUNSET) },
                        colorTop = Color.White,
                        colorBottom = SunsetPrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.LAVENDER,
                        onClick = { viewModel.setColorPalette(ColorPalette.LAVENDER) },
                        colorTop = Color.White,
                        colorBottom = LavenderPrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.MINT,
                        onClick = { viewModel.setColorPalette(ColorPalette.MINT) },
                        colorTop = Color.White,
                        colorBottom = MintPrimary
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.GOLD,
                        onClick = { viewModel.setColorPalette(ColorPalette.GOLD) },
                        colorTop = Color.White,
                        colorBottom = GoldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AniList Sync Settings
            SettingsSection(title = "Anime Discovery") {
                SettingsClickableRow(
                    label = "History",
                    description = "View your recently watched anime",
                    icon = ImageVector.vectorResource(R.drawable.history),
                    onClick = onHistoryClick
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                SettingsClickableRow(
                    label = "Title Language",
                    value = titleLanguage.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showTitleDialog = true }
                )

                SettingsClickableRow(
                    label = "Staff Name Language",
                    value = staffLanguage.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showStaffDialog = true }
                )

                SettingsClickableRow(
                    label = "Score Format",
                    value = scoreFormat.name.replace("POINT_", "Point ").replace("_", ".").lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showScoreDialog = true }
                )

                SettingsClickableRow(
                    label = "Airing Format",
                    value = airingFormat.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showAiringDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                SettingsSwitch(
                    label = "Adult Content",
                    description = "Show R18+ media in search and lists",
                    checked = adultContent,
                    onCheckedChange = { viewModel.setAdultContent(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSwitch(
                    label = "Airing Countdown",
                    description = "Show countdown for next episode",
                    checked = showAiringCountdown,
                    onCheckedChange = { viewModel.setShowAiringCountdown(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsSwitch(
                    label = "Notifications",
                    description = "Enable push notifications for airings",
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    icon = ImageVector.vectorResource(R.drawable.bell)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About / Developer Section
            SettingsSection(title = "About") {
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
                            .size(80.dp)
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
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.user),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Shiinoji",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Developer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /* Open GitHub */ }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.github_142_svgrepo_com),
                                contentDescription = "GitHub",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* Open Discord */ }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.discord_fill_svgrepo_com),
                                contentDescription = "Discord",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, // Discord Blurple
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Contributors",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Placeholder for future collaborators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.user),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Contributor",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Contribute on GitHub",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "API Credits",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.anilist_svgrepo_com),
                        contentDescription = "AniList",
                        tint = Color(0xFF02A9FF),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AniList API",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Providing high-quality anime data",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AniHub v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Anime Watchlist and Tracker App built with AniList API.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.checkForUpdates() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Check for Updates")
                }
            }
        }
    }
}

@Composable
fun SettingsClickableRow(
    label: String,
    value: String? = null,
    description: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option == selected,
                                onClick = {
                                    onSelect(option)
                                    onDismiss()
                                }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = {
                                onSelect(option)
                                onDismiss()
                            }
                        )
                        Text(
                            text = labelProvider(option),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsSwitch(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = {
                Icon(
                    imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ThemeOption(
    selected: Boolean,
    onClick: () -> Unit,
    color: Color? = null,
    icon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(color ?: MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) icon()
    }
}

@Composable
fun PaletteOption(
    selected: Boolean,
    onClick: () -> Unit,
    colorTop: Color? = null,
    colorBottom: Color? = null,
    icon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) icon()
        if (colorTop != null && colorBottom != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.5f to colorTop,
                            0.5f to colorBottom
                        )
                    )
            )
        }
    }
}
