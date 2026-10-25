package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.NotificationItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.SwapItViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: SwapItViewModel,
    onBack: () -> Unit,
    onNavigateToSwaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                            Text("Mark All Read", color = SwapItGreenPrimary, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notifications yet.\nOffers and message alerts will be delivered here.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notif ->
                    NotificationCard(
                        notif = notif,
                        onClick = {
                            viewModel.markNotificationRead(notif.id)
                            if (notif.referenceType == "offer" || notif.referenceType == "transaction") {
                                onNavigateToSwaps()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notif: NotificationItem, onClick: () -> Unit) {
    val (icon, iconTint, iconBg) = when (notif.type) {
        "OFFER_RECEIVED" -> Triple(Icons.Filled.SwapHoriz, SwapItGreenPrimary, SwapItGreenContainer)
        "OFFER_ACCEPTED", "SWAP_COMPLETED" -> Triple(Icons.Filled.CheckCircle, SwapItGreenPrimary, SwapItGreenContainer)
        "OFFER_DECLINED" -> Triple(Icons.Filled.Cancel, Color(0xFFDC2626), Color(0xFFFEE2E2))
        "COUNTER_OFFER" -> Triple(Icons.Filled.Balance, SwapItAmberTertiary, SwapItAmberContainer)
        "CONFIRMATION_NEEDED" -> Triple(Icons.Filled.HourglassEmpty, SwapItAmberTertiary, SwapItAmberContainer)
        "DISPUTE" -> Triple(Icons.Filled.Warning, Color(0xFFDC2626), Color(0xFFFEE2E2))
        else -> Triple(Icons.Filled.Notifications, SwapItGreenPrimary, SwapItGreenContainer)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notif_item_${notif.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else SwapItGreenContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notif.title,
                        fontWeight = if (notif.isRead) FontWeight.SemiBold else FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = formatShortTime(notif.createdAt),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notif.body,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            if (!notif.isRead) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = SwapItGreenPrimary,
                    modifier = Modifier.size(8.dp)
                ) {}
            }
        }
    }
}

fun formatShortTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val hours = diff / (1000 * 60 * 60)
    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "${hours}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }
}
