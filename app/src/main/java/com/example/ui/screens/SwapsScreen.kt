package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ListingItem
import com.example.data.model.SwapOffer
import com.example.data.model.SwapTransaction
import com.example.data.model.UserProfile
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatNaira
import com.example.ui.theme.*
import com.example.ui.viewmodel.SwapItViewModel

@Composable
fun SwapsScreen(
    viewModel: SwapItViewModel,
    onOpenChat: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    val offers by viewModel.userOffers.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Offers, 1: Active Swaps

    // Modals
    var counterOfferTarget by remember { mutableStateOf<SwapOffer?>(null) }
    var reviewTargetTransaction by remember { mutableStateOf<SwapTransaction?>(null) }
    var disputeTargetTransaction by remember { mutableStateOf<SwapTransaction?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Selector
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SwapItGreenPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Swap Offers", fontWeight = FontWeight.Bold)
                            if (offers.any { it.status == "Pending" && it.recipientId == currentUserId }) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SwapItAmberTertiary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                    },
                    modifier = Modifier.testTag("swaps_offers_tab")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active Exchanges (${transactions.size})", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("swaps_transactions_tab")
                )
            }
        }

        if (selectedTab == 0) {
            // Offers List
            if (offers.isEmpty()) {
                EmptySwapsState(
                    icon = Icons.Outlined.SwapHoriz,
                    title = "No Swap Offers Yet",
                    desc = "When you send or receive swap offers for your items, they will appear here with full counter-offer options."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(offers) { offer ->
                        OfferCardItem(
                            offer = offer,
                            currentUserId = currentUserId,
                            viewModel = viewModel,
                            onCounterClick = { counterOfferTarget = offer },
                            onChatClick = { otherUserId, listingId -> onOpenChat(otherUserId, listingId) }
                        )
                    }
                }
            }
        } else {
            // Transactions List
            if (transactions.isEmpty()) {
                EmptySwapsState(
                    icon = Icons.Outlined.Handshake,
                    title = "No Active Exchanges",
                    desc = "Once a swap offer is accepted, it transitions to an active exchange with dual-confirmation tracking and safe handover arrangements."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(transactions) { trans ->
                        TransactionCardItem(
                            transaction = trans,
                            currentUserId = currentUserId,
                            viewModel = viewModel,
                            onConfirmHandover = { viewModel.confirmHandover(trans.id) },
                            onDisputeClick = { disputeTargetTransaction = trans },
                            onReviewClick = { reviewTargetTransaction = trans },
                            onChatClick = { otherUserId -> onOpenChat(otherUserId, null) }
                        )
                    }
                }
            }
        }
    }

    // Counter Offer Modal
    counterOfferTarget?.let { parentOffer ->
        CounterOfferDialog(
            parentOffer = parentOffer,
            viewModel = viewModel,
            onDismiss = { counterOfferTarget = null },
            onSuccess = { counterOfferTarget = null }
        )
    }

    // Dispute Modal
    disputeTargetTransaction?.let { trans ->
        DisputeDialog(
            transaction = trans,
            viewModel = viewModel,
            onDismiss = { disputeTargetTransaction = null }
        )
    }

    // Review Modal
    reviewTargetTransaction?.let { trans ->
        ReviewDialog(
            transaction = trans,
            currentUserId = currentUserId,
            viewModel = viewModel,
            onDismiss = { reviewTargetTransaction = null }
        )
    }
}

@Composable
fun OfferCardItem(
    offer: SwapOffer,
    currentUserId: String,
    viewModel: SwapItViewModel,
    onCounterClick: () -> Unit,
    onChatClick: (String, String) -> Unit
) {
    var offeredItem by remember { mutableStateOf<ListingItem?>(null) }
    var requestedItem by remember { mutableStateOf<ListingItem?>(null) }
    var senderUser by remember { mutableStateOf<UserProfile?>(null) }
    var recipientUser by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(offer) {
        offeredItem = viewModel.repository.getListing(offer.offeredListingId)
        requestedItem = viewModel.repository.getListing(offer.requestedListingId)
        senderUser = viewModel.repository.getUser(offer.senderId)
        recipientUser = viewModel.repository.getUser(offer.recipientId)
    }

    val isIncoming = offer.recipientId == currentUserId
    val isPending = offer.status == "Pending"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offer_card_${offer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Status & Direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isIncoming) SwapItGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isIncoming) "Incoming Offer" else "Outgoing Offer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncoming) SwapItGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                StatusBadge(status = offer.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two-Item Comparison Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item 1: Offered
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = offeredItem?.primaryImageUrl ?: "",
                        contentDescription = offeredItem?.title ?: "Offered",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = offeredItem?.title ?: "Loading item...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = formatNaira(offeredItem?.estimatedValueNaira ?: 0L),
                        fontSize = 11.sp,
                        color = SwapItGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "Offered by ${senderUser?.displayName ?: "User"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Middle Exchange Arrow
                Surface(
                    shape = CircleShape,
                    color = SwapItGreenContainer,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Exchange",
                        tint = SwapItGreenPrimary,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                // Item 2: Requested
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = requestedItem?.primaryImageUrl ?: "",
                        contentDescription = requestedItem?.title ?: "Requested",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = requestedItem?.title ?: "Loading item...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = formatNaira(requestedItem?.estimatedValueNaira ?: 0L),
                        fontSize = 11.sp,
                        color = SwapItGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "Target Item", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Cash difference banner if applicable
            if (offer.cashAmount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = SwapItAmberContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CurrencyExchange,
                            contentDescription = null,
                            tint = SwapItAmberTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val payerName = if (offer.cashPayerId == currentUserId) "You" else (senderUser?.displayName ?: "The other party")
                        Text(
                            text = "Cash Top-up: $payerName will add ${formatNaira(offer.cashAmount)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SwapItOnTertiaryContainer
                        )
                    }
                }
            }

            // Proposal Message
            if (offer.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "\"${offer.message}\"",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Recipient or Sender Action Buttons
            if (isPending) {
                Spacer(modifier = Modifier.height(14.dp))
                if (isIncoming) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCounterClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("counter_offer_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Counter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.declineOffer(offer.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Text("Decline", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.acceptOffer(offer) { transId ->
                                    // Accepted
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp)
                                .testTag("accept_offer_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary)
                        ) {
                            Text("Accept 🤝", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.withdrawOffer(offer.id) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Text("Withdraw Offer")
                        }

                        Button(
                            onClick = {
                                val otherId = if (isIncoming) offer.senderId else offer.recipientId
                                onChatClick(otherId, offer.requestedListingId)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary)
                        ) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat with Trader", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCardItem(
    transaction: SwapTransaction,
    currentUserId: String,
    viewModel: SwapItViewModel,
    onConfirmHandover: () -> Unit,
    onDisputeClick: () -> Unit,
    onReviewClick: () -> Unit,
    onChatClick: (String) -> Unit
) {
    var itemOne by remember { mutableStateOf<ListingItem?>(null) }
    var itemTwo by remember { mutableStateOf<ListingItem?>(null) }
    var userOne by remember { mutableStateOf<UserProfile?>(null) }
    var userTwo by remember { mutableStateOf<UserProfile?>(null) }
    var hasUserReviewed by remember { mutableStateOf(false) }

    LaunchedEffect(transaction) {
        itemOne = viewModel.repository.getListing(transaction.listingOneId)
        itemTwo = viewModel.repository.getListing(transaction.listingTwoId)
        userOne = viewModel.repository.getUser(transaction.participantOneId)
        userTwo = viewModel.repository.getUser(transaction.participantTwoId)
        hasUserReviewed = viewModel.repository.hasReviewed(transaction.id, currentUserId)
    }

    val isUserOne = currentUserId == transaction.participantOneId
    val userConfirmed = if (isUserOne) transaction.participantOneConfirmed else transaction.participantTwoConfirmed
    val partnerConfirmed = if (isUserOne) transaction.participantTwoConfirmed else transaction.participantOneConfirmed
    val otherUserId = if (isUserOne) transaction.participantTwoId else transaction.participantOneId
    val isCompleted = transaction.status == "Completed"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_card_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exchange Agreement #${transaction.id.takeLast(4).uppercase()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = SwapItGreenPrimary
                )
                StatusBadge(status = transaction.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Swapped items summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = itemOne?.primaryImageUrl ?: "",
                    contentDescription = itemOne?.title,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = itemOne?.title ?: "Item 1", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = "from ${userOne?.displayName ?: "User"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = SwapItGreenPrimary, modifier = Modifier.padding(horizontal = 6.dp))

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = itemTwo?.title ?: "Item 2", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = "from ${userTwo?.displayName ?: "User"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = itemTwo?.primaryImageUrl ?: "",
                    contentDescription = itemTwo?.title,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (transaction.cashAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SwapItAmberContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cash Top-up: ${formatNaira(transaction.cashAmount)} agreed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SwapItOnTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Dual Confirmation Tracker
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Dual Handover Confirmation Tracker:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ConfirmationChip(
                            label = "Your Confirmation",
                            confirmed = userConfirmed
                        )
                        ConfirmationChip(
                            label = "Partner Confirmation",
                            confirmed = partnerConfirmed
                        )
                    }
                }
            }

            if (transaction.handoverNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Handover Plan: ${transaction.handoverNotes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            if (!isCompleted && transaction.status != "Disputed") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onChatClick(otherUserId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onConfirmHandover,
                        enabled = !userConfirmed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userConfirmed) Color.Gray else SwapItGreenPrimary
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(42.dp)
                            .testTag("confirm_handover_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userConfirmed) "Handover Confirmed" else "Confirm Handover",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dispute trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDisputeClick) {
                        Text("Report Issue / Dispute", fontSize = 11.sp, color = Color(0xFFDC2626))
                    }
                }
            } else if (isCompleted) {
                // Completed Swap actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SwapItGreenPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Swap Fully Completed! 🌟", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SwapItGreenPrimary)
                    }

                    if (!hasUserReviewed) {
                        Button(
                            onClick = onReviewClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SwapItAmberTertiary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("leave_review_btn")
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Review Trader", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            color = SwapItGreenContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Reviewed ✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SwapItGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else if (transaction.status == "Disputed") {
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dispute: ${transaction.disputeReason ?: "Under moderation review."}",
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmationChip(label: String, confirmed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (confirmed) Icons.Filled.CheckCircle else Icons.Outlined.HourglassEmpty,
            contentDescription = null,
            tint = if (confirmed) SwapItGreenPrimary else SwapItAmberTertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ${if (confirmed) "Confirmed ✓" else "Pending"}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (confirmed) SwapItGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CounterOfferDialog(
    parentOffer: SwapOffer,
    viewModel: SwapItViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val userListings by viewModel.userAvailableListingsForOffer.collectAsState()
    var selectedListingId by remember { mutableStateOf(userListings.firstOrNull()?.id ?: parentOffer.offeredListingId) }
    var cashAmountInput by remember { mutableStateOf(parentOffer.cashAmount.toString()) }
    var counterMessage by remember { mutableStateOf("Here are adjusted terms for the exchange.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Propose Counter-Offer ⚖️", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Propose an alternative item from your closet or adjust the cash difference. The original offer will be preserved in history.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Item to Offer:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                userListings.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedListingId = item.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedListingId == item.id, onClick = { selectedListingId = item.id })
                        Text(text = "${item.title} (${formatNaira(item.estimatedValueNaira)})", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = cashAmountInput,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) cashAmountInput = it },
                    label = { Text("Adjusted Cash Amount (₦)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = counterMessage,
                    onValueChange = { counterMessage = it },
                    label = { Text("Message explaining counter terms") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cash = cashAmountInput.toLongOrNull() ?: 0L
                    viewModel.counterOffer(
                        parentOfferId = parentOffer.id,
                        newOfferedListingId = selectedListingId,
                        newCashAmount = cash,
                        newCashPayerId = viewModel.currentUserId.value,
                        counterMessage = counterMessage,
                        onSuccess = { onSuccess() }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary)
            ) {
                Text("Send Counter-Offer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DisputeDialog(
    transaction: SwapTransaction,
    viewModel: SwapItViewModel,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Transaction Dispute ⚠️", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "If the other party did not show up, item condition was falsified, or cash was not provided, describe the issue below. SwapIt moderators will review the transaction history.",
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Describe the dispute reason") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        viewModel.fileDispute(transaction.id, reason)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Submit Dispute")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReviewDialog(
    transaction: SwapTransaction,
    currentUserId: String,
    viewModel: SwapItViewModel,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val partnerId = if (currentUserId == transaction.participantOneId) transaction.participantTwoId else transaction.participantOneId

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave a Swap Review ⭐", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How was your swapping experience? Your honest review helps keep the Nigerian SwapIt community safe and trustworthy.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Rating Stars
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$i Stars",
                                tint = SwapItAmberTertiary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Write feedback (punctuality, item condition, safe handover)...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitReview(
                        transactionId = transaction.id,
                        revieweeId = partnerId,
                        rating = rating,
                        comment = comment.ifBlank { "Great swap experience!" }
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary)
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        }
    )
}

@Composable
fun EmptySwapsState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = SwapItGreenContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SwapItGreenPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
