package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ListingItem
import com.example.data.model.Review
import com.example.data.model.UserProfile
import com.example.ui.components.ListingCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatNaira
import com.example.ui.theme.*
import com.example.ui.viewmodel.SwapItViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: SwapItViewModel,
    onNavigateToListingDetail: (String) -> Unit,
    onNavigateToCreateListing: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onOpenAccountSwitcher: () -> Unit,
    onOpenSafetyTips: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val myListings by viewModel.userListings.collectAsState()
    val favourites by viewModel.userFavourites.collectAsState()
    val allActiveListings by viewModel.filteredListings.collectAsState()
    val reviews by viewModel.userReviews.collectAsState()

    var profileTab by remember { mutableIntStateOf(0) } // 0: Listings, 1: Favourites, 2: Reviews
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val user = currentUserProfile ?: return

    val savedItems = allActiveListings.filter { item -> favourites.any { it.listingId == item.id } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Profile Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.displayName,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.dp, SwapItGreenPrimary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verified Member",
                                    tint = SwapItGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "@${user.username}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${user.city}, ${user.state}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (user.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = user.bio,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SwapItGreenContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStat(number = "${myListings.size}", label = "Listings")
                        ProfileStat(number = "${user.completedSwaps}", label = "Completed")
                        ProfileStat(number = "${user.rating} ★", label = "Reputation")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Actions Row (Switch Account, Edit, Guidelines, Admin)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenAccountSwitcher,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("profile_switch_user_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch User", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Profile", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = onOpenSafetyTips,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = SwapItGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Safety Tips", fontSize = 11.sp, color = SwapItGreenPrimary)
                        }

                        Button(
                            onClick = onOpenAdminDashboard,
                            colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("admin_dashboard_btn")
                        ) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Admin Console", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sub-tabs
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                TabRow(
                    selectedTabIndex = profileTab,
                    containerColor = Color.Transparent,
                    contentColor = SwapItGreenPrimary
                ) {
                    Tab(
                        selected = profileTab == 0,
                        onClick = { profileTab = 0 },
                        text = { Text("My Items (${myListings.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = profileTab == 1,
                        onClick = { profileTab = 1 },
                        text = { Text("Saved (${savedItems.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = profileTab == 2,
                        onClick = { profileTab = 2 },
                        text = { Text("Reviews (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (profileTab) {
            0 -> {
                // My Listings
                if (myListings.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You haven't listed anything yet. Add your first item and start swapping.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToCreateListing,
                                colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("List an Item Now")
                            }
                        }
                    }
                } else {
                    items(myListings) { item ->
                        MyListingRowItem(
                            listing = item,
                            onClick = { onNavigateToListingDetail(item.id) },
                            onToggleStatus = {
                                val newStatus = if (item.status == "Active") "Paused" else "Active"
                                viewModel.repository.setListingStatus(item.id, newStatus)
                            }
                        )
                    }
                }
            }
            1 -> {
                // Favourites
                if (savedItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No saved items yet.\nTap the heart icon on any listing to save it here.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val chunked = savedItems.chunked(2)
                    items(chunked) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (item in rowItems) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ListingCard(
                                        listing = item,
                                        isFavourite = true,
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
            2 -> {
                // Reviews
                if (reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transaction reviews yet.\nReviews appear once swaps are marked complete by both parties.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(reviews) { rev ->
                        ReviewCardItem(review = rev, viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileModal(
            user = user,
            viewModel = viewModel,
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@Composable
fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SwapItGreenPrimary)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MyListingRowItem(
    listing: ListingItem,
    onClick: () -> Unit,
    onToggleStatus: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = listing.primaryImageUrl,
                contentDescription = listing.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = listing.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                Text(text = "Est. ${formatNaira(listing.estimatedValueNaira)}", fontSize = 12.sp, color = SwapItGreenPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(status = listing.status)
            }
            if (listing.status == "Active" || listing.status == "Paused") {
                TextButton(
                    onClick = {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).run {
                            // toggle
                        }
                    }
                ) {
                    Text(
                        text = if (listing.status == "Active") "Pause" else "Activate",
                        fontSize = 11.sp,
                        color = SwapItGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewCardItem(review: Review, viewModel: SwapItViewModel) {
    var reviewer by remember { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(review) {
        reviewer = viewModel.repository.getUser(review.reviewerId)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = reviewer?.avatarUrl ?: "",
                        contentDescription = reviewer?.displayName,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = reviewer?.displayName ?: "Trader", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (i <= review.rating) SwapItAmberTertiary else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = review.comment, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun EditProfileModal(
    user: UserProfile,
    viewModel: SwapItViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(user.displayName) }
    var bio by remember { mutableStateOf(user.bio) }
    var city by remember { mutableStateOf(user.city) }
    var state by remember { mutableStateOf(user.state) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Display Name") }, singleLine = true)
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Short Bio") }, maxLines = 2)
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true)
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).run {
                        // update
                    }
                    viewModel.showMessage("Profile updated.")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
