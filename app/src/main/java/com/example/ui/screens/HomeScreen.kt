package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Category
import com.example.data.model.ListingItem
import com.example.ui.components.ListingCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.SwapItViewModel

@Composable
fun HomeScreen(
    viewModel: SwapItViewModel,
    onNavigateToExplore: (String?) -> Unit,
    onNavigateToListingDetail: (String) -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onShowSafetyTips: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val featuredListings by viewModel.featuredListings.collectAsState()
    val activeListings by viewModel.filteredListings.collectAsState()
    val favourites by viewModel.userFavourites.collectAsState()

    var homeSearchText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SwapItGreenPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapCalls,
                                contentDescription = null,
                                tint = SwapItAmberContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NIGERIA'S SMART SWAPPING MARKETPLACE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Your Next Favourite Thing Might Be Someone Else's Old One.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Discover items you love, swap what you no longer need, with or without cash difference. Transparent, social, and safer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Banner Illustration
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.swapit_hero_banner),
                            contentDescription = "SwapIt Hero Visual",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToExplore(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = SwapItGreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_explore_button")
                        ) {
                            Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Explore Items", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToCreateListing,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color.White)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_start_swapping_button")
                        ) {
                            Icon(Icons.Filled.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("List an Item", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Search Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                OutlinedTextField(
                    value = homeSearchText,
                    onValueChange = {
                        homeSearchText = it
                        viewModel.searchQuery.value = it
                    },
                    placeholder = {
                        Text(
                            "Search phones, laptops, sneakers, furniture...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = SwapItGreenPrimary
                        )
                    },
                    trailingIcon = {
                        if (homeSearchText.isNotBlank()) {
                            IconButton(onClick = {
                                homeSearchText = ""
                                viewModel.searchQuery.value = ""
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        } else {
                            IconButton(onClick = { onNavigateToExplore(null) }) {
                                Icon(Icons.Filled.Tune, contentDescription = "Filter", tint = SwapItGreenPrimary)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SwapItGreenPrimary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        // Categories Carousel
        item {
            Column(modifier = Modifier.padding(top = 18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigateToExplore(null) }) {
                        Text("See All", color = SwapItGreenPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { category ->
                        CategoryCardItem(
                            category = category,
                            onClick = { onNavigateToExplore(category.id) }
                        )
                    }
                }
            }
        }

        // How It Works Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SwapItGreenContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = SwapItGreenPrimary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How SwapIt Works (3 Simple Steps)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SwapItGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StepCard(number = "1", title = "List", desc = "Upload photos & describe items you own", modifier = Modifier.weight(1f))
                        StepCard(number = "2", title = "Swap", desc = "Discover items & send offer with or without cash", modifier = Modifier.weight(1f))
                        StepCard(number = "3", title = "Exchange", desc = "Safe meetup & dual confirmation", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Featured & Recently Listed Items
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recently Listed Swaps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active items available across Lagos, Abuja, Oyo & Rivers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Responsive Grid in LazyColumn (2 items per row)
        val chunkedListings = activeListings.chunked(2)
        items(chunkedListings) { rowItems ->
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

        // Trust, Safety & Nigerian Verification Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onShowSafetyTips() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SwapItGreenContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SwapItGreenPrimary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Safe Swapping In Nigeria 🇳🇬",
                            fontWeight = FontWeight.Bold,
                            color = SwapItGreenPrimary,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Read our tips: Public mall meetups, device testing, and dual confirmation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SwapItGreenOnContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Read tips",
                        tint = SwapItGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCardItem(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .clickable { onClick() }
            .testTag("cat_card_${category.slug}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = SwapItGreenContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.iconKey),
                    contentDescription = category.name,
                    tint = SwapItGreenPrimary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun StepCard(number: String, title: String, desc: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Surface(
                shape = CircleShape,
                color = SwapItGreenPrimary,
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 13.sp)
        }
    }
}

fun getCategoryIcon(iconKey: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconKey) {
        "smartphone" -> Icons.Filled.Smartphone
        "laptop" -> Icons.Filled.Laptop
        "checkroom" -> Icons.Filled.Checkroom
        "sports_kabaddi" -> Icons.Filled.DirectionsRun
        "watch" -> Icons.Filled.Watch
        "chair" -> Icons.Filled.Chair
        "devices_other" -> Icons.Filled.DevicesOther
        "sports_esports" -> Icons.Filled.SportsEsports
        "fitness_center" -> Icons.Filled.FitnessCenter
        "pedal_bike" -> Icons.Filled.PedalBike
        "menu_book" -> Icons.Filled.MenuBook
        "child_care" -> Icons.Filled.ChildCare
        "kitchen" -> Icons.Filled.Kitchen
        "photo_camera" -> Icons.Filled.PhotoCamera
        else -> Icons.Filled.Category
    }
}
