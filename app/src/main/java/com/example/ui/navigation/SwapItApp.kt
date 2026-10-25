package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ListingItem
import com.example.ui.components.AccountSwitcherModal
import com.example.ui.components.SafetyGuidelinesModal
import com.example.ui.components.SwapItTopBar
import com.example.ui.screens.*
import com.example.ui.theme.SwapItGreenContainer
import com.example.ui.theme.SwapItGreenPrimary
import com.example.ui.viewmodel.SwapItViewModel

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Explore : Screen("explore", "Explore")
    object CreateListing : Screen("create_listing", "List Item")
    object Swaps : Screen("swaps", "Swaps")
    object Profile : Screen("profile", "Profile")
    object Conversations : Screen("conversations", "Messages")
    object Notifications : Screen("notifications", "Notifications")
    object AdminDashboard : Screen("admin_dashboard", "Admin")
    data class ListingDetail(val listingId: String) : Screen("listing_detail", "Detail")
    data class Chat(val conversationId: String) : Screen("chat", "Chat")
}

@Composable
fun SwapItApp(
    viewModel: SwapItViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val screenStack = remember { mutableStateListOf<Screen>(Screen.Home) }

    fun navigateTo(screen: Screen) {
        screenStack.add(screen)
        currentScreen = screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            currentScreen = screenStack.last()
        } else {
            currentScreen = Screen.Home
        }
    }

    BackHandler(enabled = screenStack.size > 1 || currentScreen != Screen.Home) {
        navigateBack()
    }

    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val unreadNotifCount by viewModel.unreadNotifCount.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    // Interactive Modals
    var showAccountSwitcher by remember { mutableStateOf(false) }
    var showSafetyGuidelines by remember { mutableStateOf(false) }
    var makeOfferTargetListing by remember { mutableStateOf<ListingItem?>(null) }
    var reportTarget by remember { mutableStateOf<Pair<String, String>?>(null) }

    val isRootBottomScreen = currentScreen in listOf(
        Screen.Home, Screen.Explore, Screen.CreateListing, Screen.Swaps, Screen.Profile
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isRootBottomScreen) {
                SwapItTopBar(
                    title = "SwapIt",
                    onNotificationsClick = { navigateTo(Screen.Notifications) },
                    unreadNotifications = unreadNotifCount,
                    onChatClick = { navigateTo(Screen.Conversations) },
                    onAccountClick = { showAccountSwitcher = true },
                    currentUser = currentUserProfile
                )
            }
        },
        bottomBar = {
            if (isRootBottomScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Home,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Home)
                            currentScreen = Screen.Home
                        },
                        icon = {
                            Icon(
                                if (currentScreen == Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SwapItGreenPrimary,
                            indicatorColor = SwapItGreenContainer
                        ),
                        modifier = Modifier.testTag("nav_home_item")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Explore,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Explore)
                            currentScreen = Screen.Explore
                        },
                        icon = {
                            Icon(
                                if (currentScreen == Screen.Explore) Icons.Filled.Explore else Icons.Outlined.Explore,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SwapItGreenPrimary,
                            indicatorColor = SwapItGreenContainer
                        ),
                        modifier = Modifier.testTag("nav_explore_item")
                    )

                    // Prominent Center "List Item" Button (Master Prompt Section 5)
                    NavigationBarItem(
                        selected = currentScreen == Screen.CreateListing,
                        onClick = {
                            screenStack.add(Screen.CreateListing)
                            currentScreen = Screen.CreateListing
                        },
                        icon = {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = SwapItGreenPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "List an Item",
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        },
                        label = { Text("List Item", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SwapItGreenPrimary) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_list_item")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Swaps,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Swaps)
                            currentScreen = Screen.Swaps
                        },
                        icon = {
                            Icon(
                                if (currentScreen == Screen.Swaps) Icons.Filled.SwapHoriz else Icons.Outlined.SwapHoriz,
                                contentDescription = "Swaps"
                            )
                        },
                        label = { Text("Swaps", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SwapItGreenPrimary,
                            indicatorColor = SwapItGreenContainer
                        ),
                        modifier = Modifier.testTag("nav_swaps_item")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Profile,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Profile)
                            currentScreen = Screen.Profile
                        },
                        icon = {
                            Icon(
                                if (currentScreen == Screen.Profile) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SwapItGreenPrimary,
                            indicatorColor = SwapItGreenContainer
                        ),
                        modifier = Modifier.testTag("nav_profile_item")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToExplore = { catId ->
                            viewModel.selectedCategoryId.value = catId
                            navigateTo(Screen.Explore)
                        },
                        onNavigateToListingDetail = { id -> navigateTo(Screen.ListingDetail(id)) },
                        onNavigateToCreateListing = { navigateTo(Screen.CreateListing) },
                        onShowSafetyTips = { showSafetyGuidelines = true },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                is Screen.Explore -> {
                    ExploreScreen(
                        viewModel = viewModel,
                        onNavigateToListingDetail = { id -> navigateTo(Screen.ListingDetail(id)) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                is Screen.CreateListing -> {
                    CreateListingScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() },
                        onListingCreated = { newId ->
                            screenStack.remove(Screen.CreateListing)
                            navigateTo(Screen.ListingDetail(newId))
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                is Screen.Swaps -> {
                    SwapsScreen(
                        viewModel = viewModel,
                        onOpenChat = { otherUserId, listingId ->
                            viewModel.openOrCreateChat(otherUserId, listingId) { convId ->
                                navigateTo(Screen.Chat(convId))
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                is Screen.Profile -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToListingDetail = { id -> navigateTo(Screen.ListingDetail(id)) },
                        onNavigateToCreateListing = { navigateTo(Screen.CreateListing) },
                        onOpenAdminDashboard = { navigateTo(Screen.AdminDashboard) },
                        onOpenAccountSwitcher = { showAccountSwitcher = true },
                        onOpenSafetyTips = { showSafetyGuidelines = true },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                is Screen.ListingDetail -> {
                    ListingDetailScreen(
                        listingId = screen.listingId,
                        viewModel = viewModel,
                        onBack = { navigateBack() },
                        onOpenMakeOffer = { targetItem ->
                            makeOfferTargetListing = targetItem
                        },
                        onOpenChat = { otherUserId, listingId ->
                            viewModel.openOrCreateChat(otherUserId, listingId) { convId ->
                                navigateTo(Screen.Chat(convId))
                            }
                        },
                        onOpenReport = { type, id ->
                            reportTarget = type to id
                        }
                    )
                }
                is Screen.Conversations -> {
                    ConversationsListScreen(
                        viewModel = viewModel,
                        onOpenConversation = { convId -> navigateTo(Screen.Chat(convId)) },
                        onBack = { navigateBack() }
                    )
                }
                is Screen.Chat -> {
                    ChatScreen(
                        conversationId = screen.conversationId,
                        viewModel = viewModel,
                        onBack = { navigateBack() },
                        onNavigateToListing = { lid -> navigateTo(Screen.ListingDetail(lid)) }
                    )
                }
                is Screen.Notifications -> {
                    NotificationsScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() },
                        onNavigateToSwaps = {
                            screenStack.clear()
                            screenStack.add(Screen.Swaps)
                            currentScreen = Screen.Swaps
                        }
                    )
                }
                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() }
                    )
                }
            }
        }
    }

    // Make Offer Bottom Sheet
    makeOfferTargetListing?.let { target ->
        MakeOfferSheet(
            targetListing = target,
            viewModel = viewModel,
            onDismiss = { makeOfferTargetListing = null },
            onOfferSent = { offerId ->
                makeOfferTargetListing = null
                screenStack.clear()
                screenStack.add(Screen.Swaps)
                currentScreen = Screen.Swaps
            },
            onCreateNewListing = {
                makeOfferTargetListing = null
                navigateTo(Screen.CreateListing)
            }
        )
    }

    // Account Switcher Dialog
    if (showAccountSwitcher) {
        AccountSwitcherModal(
            users = allUsers,
            currentUserId = currentUserProfile?.id ?: "user_emeka",
            onSelectUser = { uid ->
                viewModel.switchActiveUser(uid)
            },
            onDismiss = { showAccountSwitcher = false }
        )
    }

    // Safety Guidelines Modal
    if (showSafetyGuidelines) {
        SafetyGuidelinesModal(
            onDismiss = { showSafetyGuidelines = false }
        )
    }

    // Report Dialog
    reportTarget?.let { (type, targetId) ->
        var reportReason by remember { mutableStateOf("") }
        var reportDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { reportTarget = null },
            title = { Text("Report $type 🚩", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a violation reason and describe what makes this suspicious:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Reason (e.g. Counterfeit item, Suspicious behavior)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("Additional details") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            viewModel.submitReport(type, targetId, reportReason, reportDesc)
                            reportTarget = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportTarget = null }) { Text("Cancel") }
            }
        )
    }
}
