package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.ConditionBadge
import com.example.ui.components.formatNaira
import com.example.ui.theme.*
import com.example.ui.viewmodel.SwapItViewModel

enum class CashDifferenceType {
    NONE, SENDER_PAYS, RECIPIENT_PAYS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeOfferSheet(
    targetListing: ListingItem,
    viewModel: SwapItViewModel,
    onDismiss: () -> Unit,
    onOfferSent: (String) -> Unit,
    onCreateNewListing: () -> Unit
) {
    val myActiveListings by viewModel.userAvailableListingsForOffer.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var selectedOfferedListingId by remember {
        mutableStateOf(myActiveListings.firstOrNull()?.id ?: "")
    }

    var cashDiffType by remember { mutableStateOf(CashDifferenceType.NONE) }
    var cashAmountInput by remember { mutableStateOf("") }
    var proposalMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("make_offer_bottom_sheet")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = null,
                        tint = SwapItGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Propose a Swap Offer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Target Item Summary
                item {
                    Text(
                        text = "1. Item You Want:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = targetListing.primaryImageUrl,
                                contentDescription = targetListing.title,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = targetListing.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Est. " + formatNaira(targetListing.estimatedValueNaira),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SwapItGreenPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Offered Item Selection (Step 2)
                item {
                    Text(
                        text = "2. Select What You're Offering in Exchange:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (myActiveListings.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SwapItAmberContainer.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SwapItAmberTertiary.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "You don't have any active items to offer yet.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SwapItOnTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onCreateNewListing()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("List an Item Now")
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            myActiveListings.forEach { item ->
                                val isSelected = item.id == selectedOfferedListingId
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedOfferedListingId = item.id }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SwapItGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    color = if (isSelected) SwapItGreenContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedOfferedListingId = item.id },
                                            colors = RadioButtonDefaults.colors(selectedColor = SwapItGreenPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        AsyncImage(
                                            model = item.primaryImageUrl,
                                            contentDescription = item.title,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Est. " + formatNaira(item.estimatedValueNaira),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SwapItGreenPrimary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                ConditionBadge(condition = item.condition)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Cash Top-up / Difference (Step 3)
                item {
                    Text(
                        text = "3. Cash Difference / Top-up (Optional):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CashOptionRow(
                            title = "Straight Item-for-Item (No cash addition)",
                            selected = cashDiffType == CashDifferenceType.NONE,
                            onClick = { cashDiffType = CashDifferenceType.NONE }
                        )
                        CashOptionRow(
                            title = "I will add cash to balance value (+ ₦)",
                            selected = cashDiffType == CashDifferenceType.SENDER_PAYS,
                            onClick = { cashDiffType = CashDifferenceType.SENDER_PAYS }
                        )
                        CashOptionRow(
                            title = "Recipient should add cash (+ ₦)",
                            selected = cashDiffType == CashDifferenceType.RECIPIENT_PAYS,
                            onClick = { cashDiffType = CashDifferenceType.RECIPIENT_PAYS }
                        )
                    }

                    if (cashDiffType != CashDifferenceType.NONE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = cashAmountInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    cashAmountInput = input
                                }
                            },
                            label = { Text("Cash Amount (in Naira ₦)") },
                            placeholder = { Text("e.g. 50000") },
                            leadingIcon = {
                                Text("₦", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SwapItAmberTertiary, modifier = Modifier.padding(start = 12.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("offer_cash_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SwapItGreenPrimary)
                        )
                    }
                }

                // Proposal Message (Step 4)
                item {
                    Text(
                        text = "4. Message & Exchange Arrangements:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = proposalMessage,
                        onValueChange = { proposalMessage = it },
                        placeholder = { Text("Explain your offer or propose a safe meetup spot (e.g. Ikeja City Mall on Saturday)...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("offer_message_input"),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SwapItGreenPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            val canSubmit = selectedOfferedListingId.isNotBlank() && !isSubmitting
            Button(
                onClick = {
                    if (!canSubmit) return@Button
                    isSubmitting = true
                    val cashAmount = cashAmountInput.toLongOrNull() ?: 0L
                    val cashPayer = when (cashDiffType) {
                        CashDifferenceType.NONE -> ""
                        CashDifferenceType.SENDER_PAYS -> currentUserId
                        CashDifferenceType.RECIPIENT_PAYS -> targetListing.ownerId
                    }

                    viewModel.sendSwapOffer(
                        offeredListingId = selectedOfferedListingId,
                        requestedListingId = targetListing.id,
                        recipientId = targetListing.ownerId,
                        cashAmount = cashAmount,
                        cashPayerId = cashPayer,
                        message = proposalMessage.ifBlank { "Proposed item swap for ${targetListing.title}" },
                        onSuccess = { offerId ->
                            isSubmitting = false
                            onDismiss()
                            onOfferSent(offerId)
                        }
                    )
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_swap_offer_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Swap Offer 🚀", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CashOptionRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (selected) SwapItGreenContainer.copy(alpha = 0.5f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = SwapItGreenPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
