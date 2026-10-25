package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ListingItem
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

fun formatNaira(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "₦" + formatter.format(amount)
}

@Composable
fun ConditionBadge(condition: String, modifier: Modifier = Modifier) {
    val (bgColor, fgColor) = when (condition) {
        "Brand New" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        "Like New" -> Color(0xFFE0F2F1) to Color(0xFF004D40)
        "Excellent" -> Color(0xFFE3F2FD) to Color(0xFF0D47A1)
        "Good" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "Fair" -> Color(0xFFFBE9E7) to Color(0xFFBF360C)
        else -> Color(0xFFECEFF1) to Color(0xFF37474F)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = condition,
            color = fgColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, fgColor) = when (status) {
        "Active" -> Color(0xFFE2F4E7) to Color(0xFF0A4C2E)
        "Pending", "Pending Exchange", "Awaiting Handover" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "Completed", "Swapped" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "Disputed" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        "Declined", "Cancelled" -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status,
            color = fgColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ListingCard(
    listing: ListingItem,
    isFavourite: Boolean,
    onFavouriteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("listing_card_${listing.id}")
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = listing.primaryImageUrl,
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Condition Pill
                ConditionBadge(
                    condition = listing.condition,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                // Favourite Button
                IconButton(
                    onClick = onFavouriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .testTag("fav_btn_${listing.id}")
                ) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFavourite) Color(0xFFDC2626) else Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Cash top-up pill if applicable
                if (listing.acceptsCashTopup) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        color = Color(0xFF042113).copy(alpha = 0.75f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CurrencyExchange,
                                contentDescription = null,
                                tint = SwapItAmberContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Cash Top-up OK",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Est. " + formatNaira(listing.estimatedValueNaira),
                        color = SwapItGreenPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (listing.isValueNegotiable) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(Negotiable)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Swap Wishlist
                if (listing.preferredItems.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SyncAlt,
                            contentDescription = null,
                            tint = SwapItSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Wants: " + listing.preferredItems,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${listing.city}, ${listing.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapItTopBar(
    title: String,
    onNotificationsClick: () -> Unit,
    unreadNotifications: Int,
    onChatClick: () -> Unit,
    onAccountClick: () -> Unit,
    currentUser: UserProfile?,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SwapItGreenPrimary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "SwapIt Logo",
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SwapItGreenPrimary
                    )
                    Text(
                        text = "Nigeria's Smart Swapping Hub",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Notifications with badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("top_bar_notif_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotifications > 0) {
                            Badge(containerColor = SwapItAmberTertiary) {
                                Text("$unreadNotifications")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Messages button
            IconButton(
                onClick = onChatClick,
                modifier = Modifier.testTag("top_bar_messages_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Messages",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // User Avatar chip / switcher
            Surface(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(CircleShape)
                    .clickable { onAccountClick() }
                    .border(1.5.dp, SwapItGreenPrimary, CircleShape)
                    .size(36.dp)
                    .testTag("user_avatar_chip"),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (currentUser != null && currentUser.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = currentUser.avatarUrl,
                        contentDescription = currentUser.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (currentUser?.displayName?.take(1) ?: "U"),
                            fontWeight = FontWeight.Bold,
                            color = SwapItGreenPrimary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

@Composable
fun AccountSwitcherModal(
    users: List<UserProfile>,
    currentUserId: String,
    onSelectUser: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.SwitchAccount,
                    contentDescription = null,
                    tint = SwapItGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch Active Account", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select any simulated Nigerian user account to test peer-to-peer swap offers, counter-offers, dual confirmations, and administration:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                users.forEach { user ->
                    val isSelected = user.id == currentUserId
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectUser(user.id)
                                onDismiss()
                            }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SwapItGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) SwapItGreenContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.displayName,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (user.role == "admin") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            color = SwapItAmberContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ADMIN",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SwapItAmberTertiary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "@${user.username} • ${user.city}, ${user.state}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Active",
                                    tint = SwapItGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SwapItGreenPrimary)
            }
        }
    )
}

@Composable
fun SafetyGuidelinesModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = SwapItGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SwapIt Safety Guidelines 🛡️", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SafetyTipRow(
                    title = "1. Meet in Busy Public Locations",
                    desc = "Always arrange exchanges at malls, public plazas, or verified hubs (e.g. Ikeja City Mall, Palms Mall, Jabi Lake Mall). Never meet in private secluded quarters."
                )
                SafetyTipRow(
                    title = "2. Inspect & Test Fully",
                    desc = "Thoroughly test electronics, inspect shoes, and verify working condition BEFORE confirming handover."
                )
                SafetyTipRow(
                    title = "3. No Advance Wire Transfers",
                    desc = "Never send cash top-ups in advance based on promises. Exchange cash physically or verify hand-to-hand during the meeting."
                )
                SafetyTipRow(
                    title = "4. Dual Confirmation System",
                    desc = "Both parties must independently press 'Confirm Handover' in the app to close the transaction safely."
                )
                SafetyTipRow(
                    title = "5. Report Suspicious Activity",
                    desc = "Use the 'Report' button on any suspicious listing, fraudulent user, or unresolved swap dispute."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary)
            ) {
                Text("Understood & Safe")
            }
        }
    )
}

@Composable
fun SafetyTipRow(title: String, desc: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SwapItGreenPrimary)
        Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
