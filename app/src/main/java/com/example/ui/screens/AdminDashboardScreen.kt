package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AdminAuditLog
import com.example.data.model.Report
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.SwapItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: SwapItViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeListingCount by viewModel.activeListingCount.collectAsState()
    val pendingOfferCount by viewModel.pendingOfferCount.collectAsState()
    val completedTransCount by viewModel.completedTransCount.collectAsState()
    val disputeCount by viewModel.disputeCount.collectAsState()
    val pendingReportCount by viewModel.pendingReportCount.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val auditLogs by viewModel.adminAuditLogs.collectAsState()

    var adminTab by remember { mutableIntStateOf(0) } // 0: Reports, 1: Users, 2: Audit Logs

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = SwapItGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SwapIt Moderation & Admin", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-Level KPI Summary (Section 19)
            item {
                Text("Platform Metrics", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiMetricCard(
                        title = "Active Listings",
                        value = "$activeListingCount",
                        icon = Icons.Filled.Storefront,
                        color = SwapItGreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Pending Offers",
                        value = "$pendingOfferCount",
                        icon = Icons.Filled.SwapHoriz,
                        color = SwapItAmberTertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiMetricCard(
                        title = "Completed Swaps",
                        value = "$completedTransCount",
                        icon = Icons.Filled.CheckCircle,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Open Disputes",
                        value = "$disputeCount",
                        icon = Icons.Filled.Warning,
                        color = Color(0xFFB91C1C),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab bar
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    TabRow(
                        selectedTabIndex = adminTab,
                        containerColor = Color.Transparent,
                        contentColor = SwapItGreenPrimary
                    ) {
                        Tab(
                            selected = adminTab == 0,
                            onClick = { adminTab = 0 },
                            text = { Text("Reports (${allReports.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = adminTab == 1,
                            onClick = { adminTab = 1 },
                            text = { Text("Users (${allUsers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = adminTab == 2,
                            onClick = { adminTab = 2 },
                            text = { Text("Audit Logs", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }
                }
            }

            when (adminTab) {
                0 -> {
                    // Reports
                    if (allReports.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No user reports currently pending review.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(allReports) { report ->
                            AdminReportCard(
                                report = report,
                                onUpdateStatus = { newStatus ->
                                    viewModel.updateReportStatus(report.id, newStatus, "Actioned by admin moderator")
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // Users Management
                    items(allUsers) { u ->
                        AdminUserCard(
                            user = u,
                            onSuspend = { viewModel.suspendUser(u.id, "Violation of SwapIt community trading guidelines") },
                            onReactivate = { viewModel.reactivateUser(u.id, "Account restored following appeal") }
                        )
                    }
                }
                2 -> {
                    // Audit Logs
                    if (auditLogs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No audit entries recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(auditLogs) { log ->
                            AdminAuditCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun AdminReportCard(
    report: Report,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Report on ${report.targetType.uppercase()}: ${report.targetId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626)
                )
                Text(
                    text = report.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (report.status == "Pending") SwapItAmberTertiary else SwapItGreenPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Reason: ${report.reason}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(text = report.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (report.status == "Pending") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onUpdateStatus("Dismissed") }) {
                        Text("Dismiss", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onUpdateStatus("Resolved") },
                        colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Mark Resolved", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserProfile,
    onSuspend: () -> Unit,
    onReactivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = user.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${user.accountStatus.uppercase()})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.accountStatus == "active") SwapItGreenPrimary else Color(0xFFDC2626)
                    )
                }
                Text(text = "@${user.username} • ${user.city}, ${user.state}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (user.role != "admin") {
                if (user.accountStatus == "active") {
                    OutlinedButton(
                        onClick = onSuspend,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                    ) {
                        Text("Suspend", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onReactivate,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SwapItGreenPrimary)
                    ) {
                        Text("Reactivate", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditCard(log: AdminAuditLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SwapItGreenPrimary)
                Text(text = log.targetType, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "Target: ${log.targetId}", fontSize = 11.sp)
            Text(text = "Reason: ${log.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
