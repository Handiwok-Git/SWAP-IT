package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.UserProfile
import com.example.ui.components.ConditionBadge
import com.example.ui.components.formatNaira
import com.example.ui.theme.*
import com.example.ui.viewmodel.SwapItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    viewModel: SwapItViewModel,
    onBack: () -> Unit,
    onOpenMakeOffer: (ListingItem) -> Unit,
    onOpenChat: (String, String) -> Unit,
    onOpenReport: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listingFlow = remember(listingId) { viewModel.repository.observeListing(listingId) }
    val listing by listingFlow.collectAsState(initial = null)
    val favourites by viewModel.userFavourites.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var owner by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(listing?.ownerId) {
        listing?.ownerId?.let { uid ->
            owner = viewModel.repository.getUser(uid)
        }
    }

    if (listing == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SwapItGreenPrimary)
        }
        return
    }

    val currentListing = listing!!
    val isOwner = currentUserId == currentListing.ownerId
    val isFav = favourites.any { it.listingId == currentListing.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listing Details", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavourite(currentListing.id) },
                        modifier = Modifier.testTag("detail_fav_button")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favourite",
                            tint = if (isFav) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onOpenReport("listing", currentListing.id) }) {
                        Icon(Icons.Outlined.Flag, contentDescription = "Report Listing", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isOwner) {
                        OutlinedButton(
                            onClick = {
                                viewModel.showMessage("This is your active listing. You can manage offers in the Swaps tab.")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Your Item (Manage in Swaps)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onOpenChat(currentListing.ownerId, currentListing.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("message_owner_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SwapItGreenPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(SwapItGreenPrimary)
                            )
                        ) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Message", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onOpenMakeOffer(currentListing) },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("make_swap_offer_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary)
                        ) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Make a Swap Offer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = currentListing.primaryImageUrl,
                    contentDescription = currentListing.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                ConditionBadge(
                    condition = currentListing.condition,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title and Estimated Value
                Text(
                    text = currentListing.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Estimated Value: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatNaira(currentListing.estimatedValueNaira),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = SwapItGreenPrimary
                    )
                    if (currentListing.isValueNegotiable) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SwapItGreenContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Negotiable",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SwapItGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Disclaimer per Prompt: Section 9 & 11
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Estimated values are user-provided guides and not guaranteed market valuations by SwapIt.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Owner Profile Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = owner?.avatarUrl ?: "",
                            contentDescription = owner?.displayName ?: "Owner",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.dp, SwapItGreenPrimary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = owner?.displayName ?: "SwapIt Member",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = SwapItGreenPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "@${owner?.username ?: "member"} • ${currentListing.city}, ${currentListing.state}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = SwapItAmberTertiary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${owner?.rating ?: 5.0} (${owner?.reviewCount ?: 0} reviews)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${owner?.completedSwaps ?: 0} Swaps Done",
                                    fontSize = 11.sp,
                                    color = SwapItGreenPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // What Owner Wants in Return (Core Swapping Requirement)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SwapItGreenContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = null,
                                tint = SwapItGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "What the Owner Wants in Return",
                                fontWeight = FontWeight.Bold,
                                color = SwapItGreenPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (currentListing.preferredItems.isNotBlank()) {
                            DetailItemRow(label = "Desired Items", value = currentListing.preferredItems)
                        }
                        if (currentListing.preferredCategories.isNotBlank()) {
                            DetailItemRow(label = "Categories", value = currentListing.preferredCategories)
                        }

                        // Cash top-up conditions
                        val cashTerms = buildList {
                            if (currentListing.acceptsCashTopup) add("Cash top-up accepted")
                            if (currentListing.willingToAddCash) add("Willing to add cash for higher value items")
                            if (currentListing.willingToReceiveCash) add("Willing to receive cash for lower value items")
                            if (currentListing.itemForItemOnly) add("Straight item-for-item preferred")
                        }.joinToString(" • ")

                        if (cashTerms.isNotBlank()) {
                            DetailItemRow(label = "Cash Terms", value = cashTerms)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentListing.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Item Specs
                Text(
                    text = "Item Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DetailItemRow("Brand", currentListing.brand)
                        DetailItemRow("Model", currentListing.model)
                        DetailItemRow("Condition", currentListing.condition)
                        DetailItemRow("Handover Method", currentListing.handoverMethod)
                        DetailItemRow("Availability", currentListing.handoverAvailability)
                        DetailItemRow("Location", "${currentListing.city}, ${currentListing.state}")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(2f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
