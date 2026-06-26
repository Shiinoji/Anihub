package com.watchlist.anihub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
            // Preview Mockup (simplified version of the image)
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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
                        colorBottom = Color(0xFF795548)
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.DEEP_BROWN,
                        onClick = { viewModel.setColorPalette(ColorPalette.DEEP_BROWN) },
                        colorTop = Color.White,
                        colorBottom = Color(0xFF5D4037)
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.PURPLE,
                        onClick = { viewModel.setColorPalette(ColorPalette.PURPLE) },
                        colorTop = Color.White,
                        colorBottom = Color(0xFF673AB7)
                    )
                    PaletteOption(
                        selected = colorPalette == ColorPalette.DEEP_PURPLE,
                        onClick = { viewModel.setColorPalette(ColorPalette.DEEP_PURPLE) },
                        colorTop = Color.White,
                        colorBottom = Color(0xFF4527A0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AniList Sync Settings
            SettingsSection(title = "Anime Discovery") {
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

            // About Section
            SettingsSection(title = "About") {
                Text(
                    text = "AniHub v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A modern anime tracker and discovery app built with Jetpack Compose.",
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
    value: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
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
            onCheckedChange = onCheckedChange
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
            .size(48.dp)
            .clip(CircleShape)
            .background(color ?: MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) icon()
        if (selected && color != null) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = if (color == Color.White) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f)
            )
        }
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
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                else Modifier
            ),
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
