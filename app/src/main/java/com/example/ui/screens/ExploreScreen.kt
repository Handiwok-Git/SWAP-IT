package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ListingCard
import com.example.ui.theme.SwapItGreenContainer
import com.example.ui.theme.SwapItGreenPrimary
import com.example.ui.viewmodel.SortOption
import com.example.ui.viewmodel.SwapItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: SwapItViewModel,
    onNavigateToListingDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listings by viewModel.filteredListings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val favourites by viewModel.userFavourites.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val selectedCondition by viewModel.selectedCondition.collectAsState()
    val selectedState by viewModel.selectedState.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val cashTopupOnly by viewModel.cashTopupOnly.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val nigerianStates = listOf("Lagos", "Abuja FCT", "Oyo", "Rivers", "Ogun", "Enugu", "Kano")
    val conditions = listOf("Brand New", "Like New", "Excellent", "Good", "Fair", "For parts or repair")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search bar with filter icon
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by brand, item, or city...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = SwapItGreenPrimary)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.testTag("explore_filter_icon")
                            ) {
                                val hasActiveFilter = selectedCondition != null || selectedState != null || cashTopupOnly
                                BadgedBox(badge = { if (hasActiveFilter) Badge(containerColor = SwapItGreenPrimary) }) {
                                    Icon(Icons.Filled.Tune, contentDescription = "Filters", tint = SwapItGreenPrimary)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SwapItGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips Carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectedCategoryId.value = null },
                            label = { Text("All Categories") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SwapItGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.id,
                            onClick = {
                                viewModel.selectedCategoryId.value = if (selectedCategory == cat.id) null else cat.id
                            },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SwapItGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Sort & Filter quick bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${listings.size} Swaps Available",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sort menu button
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(
                            onClick = { sortMenuExpanded = true },
                            modifier = Modifier.testTag("sort_menu_button")
                        ) {
                            Icon(Icons.Filled.Sort, contentDescription = null, modifier = Modifier.size(16.dp), tint = SwapItGreenPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (selectedSort) {
                                    SortOption.RECOMMENDED -> "Recommended"
                                    SortOption.NEWEST -> "Newest"
                                    SortOption.PRICE_LOW_TO_HIGH -> "Value: Low to High"
                                    SortOption.PRICE_HIGH_TO_LOW -> "Value: High to Low"
                                },
                                color = SwapItGreenPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recommended") },
                                onClick = {
                                    viewModel.selectedSort.value = SortOption.RECOMMENDED
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Newest") },
                                onClick = {
                                    viewModel.selectedSort.value = SortOption.NEWEST
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Estimated Value: Low to High") },
                                onClick = {
                                    viewModel.selectedSort.value = SortOption.PRICE_LOW_TO_HIGH
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Estimated Value: High to Low") },
                                onClick = {
                                    viewModel.selectedSort.value = SortOption.PRICE_HIGH_TO_LOW
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Empty State
            if (listings.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SwapItGreenContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SearchOff,
                                contentDescription = null,
                                tint = SwapItGreenPrimary,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Items Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try a different search term or reset your filters to discover more items to swap.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.clearFilters() },
                            colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear All Filters")
                        }
                    }
                }
            } else {
                // Responsive Grid (2 columns)
                val chunked = listings.chunked(2)
                items(chunked) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (item in rowItems) {
                            Box(modifier = Modifier.weight(1f)) {
                                val isFav = favourites.any { it.listingId == item.id }
                                ListingCard(
                                    listing = item,
                                    isFavourite = isFav,
                                    onFavouriteClick = { viewModel.toggleFavourite(item.id) },
                                    onClick = { onNavigateToListingDetail(item.id) }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Filter Swaps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Reset", color = SwapItGreenPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // State / Location
                Text("Location (State)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedState == null,
                            onClick = { viewModel.selectedState.value = null },
                            label = { Text("All Nigeria") }
                        )
                    }
                    items(nigerianStates) { st ->
                        FilterChip(
                            selected = selectedState == st,
                            onClick = { viewModel.selectedState.value = if (selectedState == st) null else st },
                            label = { Text(st) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Condition
                Text("Condition", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCondition == null,
                            onClick = { viewModel.selectedCondition.value = null },
                            label = { Text("All Conditions") }
                        )
                    }
                    items(conditions) { cond ->
                        FilterChip(
                            selected = selectedCondition == cond,
                            onClick = { viewModel.selectedCondition.value = if (selectedCondition == cond) null else cond },
                            label = { Text(cond) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cash top-up filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Accepts Cash Top-Up", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Show items open to adding or receiving cash", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = cashTopupOnly,
                        onCheckedChange = { viewModel.cashTopupOnly.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SwapItGreenPrimary, checkedTrackColor = SwapItGreenContainer)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
