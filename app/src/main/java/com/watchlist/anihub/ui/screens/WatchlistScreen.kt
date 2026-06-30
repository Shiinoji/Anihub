package com.watchlist.anihub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watchlist.anihub.R
import com.watchlist.anihub.data.local.AnimeEntity
import com.watchlist.anihub.data.local.WatchlistStatus
import com.watchlist.anihub.ui.components.SimpleAnimeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onBackClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Watchlist", "Favorites")
    
    val watchlist by viewModel.watchlist.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val itemsPerRow by viewModel.itemsPerRow.collectAsState()
    
    val currentList = if (selectedTab == 0) watchlist else favorites

    var animeToDelete by remember { mutableStateOf<AnimeEntity?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    if (animeToDelete != null) {
        AlertDialog(
            onDismissRequest = { animeToDelete = null },
            title = { Text("Remove from Watchlist?") },
            text = { Text("Are you sure you want to remove \"${animeToDelete?.title}\" from your watchlist?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        animeToDelete?.let { viewModel.removeFromWatchlist(it) }
                        animeToDelete = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { animeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Personalize View",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Filter Section (Only for Watchlist tab)
                if (selectedTab == 0) {
                    Text("Filter by Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WatchlistStatus.entries.forEach { status ->
                            FilterChip(
                                selected = filterStatus == status,
                                onClick = { 
                                    viewModel.setFilterStatus(if (filterStatus == status) null else status)
                                },
                                label = { Text(status.getDisplayName()) },
                                leadingIcon = if (filterStatus == status) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sort Section
                Text("Sort Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WatchlistSort.entries.forEach { sort ->
                        FilterChip(
                            selected = sortOrder == sort,
                            onClick = { viewModel.setSortOrder(sort) },
                            label = { 
                                Text(when(sort) {
                                    WatchlistSort.ALPHABETICAL -> "A-Z"
                                    WatchlistSort.LAST_ADDED -> "Last Added"
                                    WatchlistSort.DATE_ADDED -> "Date Added"
                                })
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Display Section
                Text("Items per Row: $itemsPerRow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Slider(
                    value = itemsPerRow.toFloat(),
                    onValueChange = { viewModel.setItemsPerRow(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Watchlist", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(ImageVector.vectorResource(R.drawable.arrow_left), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filter and Sort")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .fillMaxWidth()
                                .padding(horizontal = 64.dp) // Makes the indicator line much smaller
                                .height(3.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                )
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                text = title,
                                style = if (selectedTab == index) 
                                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                else 
                                    MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) 
                        }
                    )
                }
            }
            
            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedTab == 0) 
                                ImageVector.vectorResource(R.drawable.bookmark) 
                            else 
                                ImageVector.vectorResource(R.drawable.heart),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == 0) "Your watchlist is empty" else "No favorites yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(itemsPerRow),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList) { anime ->
                        SimpleAnimeCard(
                            title = anime.title,
                            imageUrl = anime.imageUrl,
                            onClick = { onAnimeClick(anime.id) },
                            onLongClick = { animeToDelete = anime },
                            status = if (selectedTab == 0) anime.status.getDisplayName() else null
                        )
                    }
                }
            }
        }
    }
}
