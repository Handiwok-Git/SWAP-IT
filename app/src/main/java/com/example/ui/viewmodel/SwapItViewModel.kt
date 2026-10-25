package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.SwapItRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    RECOMMENDED, NEWEST, PRICE_LOW_TO_HIGH, PRICE_HIGH_TO_LOW
}

class SwapItViewModel(application: Application) : AndroidViewModel(application) {

    val repository = SwapItRepository(application)

    // Current active user ID & profile
    val currentUserId: StateFlow<String> = repository.currentUserId
    val currentUserProfile: StateFlow<UserProfile?> = currentUserId
        .flatMapLatest { id -> repository.observeUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserProfile>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categories
    val categories: StateFlow<List<Category>> = repository.observeAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and filtering state
    val searchQuery = MutableStateFlow("")
    val selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCondition = MutableStateFlow<String?>(null)
    val selectedState = MutableStateFlow<String?>(null)
    val selectedSort = MutableStateFlow(SortOption.RECOMMENDED)
    val cashTopupOnly = MutableStateFlow(false)

    // All active listings from DB
    private val rawActiveListings = repository.observeActiveListings()
    val featuredListings: StateFlow<List<ListingItem>> = repository.observeFeaturedListings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class FilterParams(
        val query: String,
        val catId: String?,
        val condition: String?,
        val state: String?,
        val sort: SortOption,
        val cashOnly: Boolean
    )

    private val filterParams = combine(
        combine(searchQuery, selectedCategoryId) { q, cat -> q to cat },
        combine(selectedCondition, selectedState) { cond, st -> cond to st },
        combine(selectedSort, cashTopupOnly) { s, cash -> s to cash }
    ) { (q, cat), (cond, st), (s, cash) ->
        FilterParams(q, cat, cond, st, s, cash)
    }

    // Filtered listings Flow
    val filteredListings: StateFlow<List<ListingItem>> = combine(
        rawActiveListings,
        filterParams
    ) { listings, params ->
        var result = listings

        if (params.query.isNotBlank()) {
            val q = params.query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.brand.lowercase().contains(q) ||
                it.model.lowercase().contains(q) ||
                it.city.lowercase().contains(q) ||
                it.state.lowercase().contains(q)
            }
        }

        if (params.catId != null) {
            result = result.filter { it.categoryId == params.catId }
        }

        if (params.condition != null) {
            result = result.filter { it.condition == params.condition }
        }

        if (params.state != null) {
            result = result.filter { it.state == params.state }
        }

        if (params.cashOnly) {
            result = result.filter { it.acceptsCashTopup || it.willingToAddCash || it.willingToReceiveCash }
        }

        when (params.sort) {
            SortOption.RECOMMENDED -> result.sortedByDescending { it.viewsCount + (it.favouritesCount * 3) }
            SortOption.NEWEST -> result.sortedByDescending { it.createdAt }
            SortOption.PRICE_LOW_TO_HIGH -> result.sortedBy { it.estimatedValueNaira }
            SortOption.PRICE_HIGH_TO_LOW -> result.sortedByDescending { it.estimatedValueNaira }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favourites
    val userFavourites: StateFlow<List<Favourite>> = currentUserId
        .flatMapLatest { uid -> repository.observeFavourites(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User's own listings
    val userListings: StateFlow<List<ListingItem>> = currentUserId
        .flatMapLatest { uid -> repository.observeListingsByOwner(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User's active listings available for offering
    val userAvailableListingsForOffer: StateFlow<List<ListingItem>> = currentUserId
        .flatMapLatest { uid -> repository.observeListingsByOwnerAndStatus(uid, "Active") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Swap Offers
    val userOffers: StateFlow<List<SwapOffer>> = currentUserId
        .flatMapLatest { uid -> repository.observeUserOffers(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Swap Transactions
    val userTransactions: StateFlow<List<SwapTransaction>> = currentUserId
        .flatMapLatest { uid -> repository.observeUserTransactions(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Conversations
    val userConversations: StateFlow<List<Conversation>> = currentUserId
        .flatMapLatest { uid -> repository.observeConversationsForUser(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<NotificationItem>> = currentUserId
        .flatMapLatest { uid -> repository.observeNotifications(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = currentUserId
        .flatMapLatest { uid -> repository.observeUnreadNotificationCount(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // User Reviews
    val userReviews: StateFlow<List<Review>> = currentUserId
        .flatMapLatest { uid -> repository.observeReviewsForUser(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Metrics
    val activeListingCount: StateFlow<Int> = repository.observeActiveListingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingOfferCount: StateFlow<Int> = repository.observePendingOfferCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val completedTransCount: StateFlow<Int> = repository.observeCompletedTransactionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val disputeCount: StateFlow<Int> = repository.observeDisputeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingReportCount: StateFlow<Int> = repository.observePendingReportCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val allReports: StateFlow<List<Report>> = repository.observeAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val adminAuditLogs: StateFlow<List<AdminAuditLog>> = repository.observeAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Account Switcher
    fun switchActiveUser(userId: String) {
        repository.switchActiveUser(userId)
        viewModelScope.launch {
            val user = repository.getUser(userId)
            _userMessage.value = "Switched active account to ${user?.displayName ?: userId}"
        }
    }

    // Favourites
    fun toggleFavourite(listingId: String) {
        val uid = currentUserId.value
        val isFav = userFavourites.value.any { it.listingId == listingId }
        viewModelScope.launch {
            repository.toggleFavourite(uid, listingId, isFav)
            _userMessage.value = if (isFav) "Removed from saved items" else "Saved to favourites! ❤️"
        }
    }

    // Listing Operations
    fun createListing(
        title: String,
        description: String,
        categoryId: String,
        brand: String,
        model: String,
        condition: String,
        estimatedValueNaira: Long,
        isValueNegotiable: Boolean,
        state: String,
        city: String,
        lga: String,
        acceptsCashTopup: Boolean,
        willingToAddCash: Boolean,
        willingToReceiveCash: Boolean,
        itemForItemOnly: Boolean,
        preferredCategories: String,
        preferredItems: String,
        handoverMethod: String,
        handoverAvailability: String,
        primaryImageUrl: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newId = repository.createListing(
                    title, description, categoryId, brand, model, condition,
                    estimatedValueNaira, isValueNegotiable, state, city, lga,
                    acceptsCashTopup, willingToAddCash, willingToReceiveCash,
                    itemForItemOnly, preferredCategories, preferredItems,
                    handoverMethod, handoverAvailability, primaryImageUrl
                )
                _userMessage.value = "Listing published successfully! 🎉"
                onSuccess(newId)
            } catch (e: Exception) {
                _userMessage.value = "Failed to create listing: ${e.message}"
            }
        }
    }

    // Offer Operations
    fun sendSwapOffer(
        offeredListingId: String,
        requestedListingId: String,
        recipientId: String,
        cashAmount: Long,
        cashPayerId: String,
        message: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.sendSwapOffer(
                offeredListingId, requestedListingId, recipientId,
                cashAmount, cashPayerId, message
            )
            result.onSuccess { offerId ->
                _userMessage.value = "Your swap offer has been sent! We'll notify you when the owner responds. 🚀"
                onSuccess(offerId)
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to send offer"
            }
        }
    }

    fun counterOffer(
        parentOfferId: String,
        newOfferedListingId: String,
        newCashAmount: Long,
        newCashPayerId: String,
        counterMessage: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.counterOffer(
                parentOfferId, newOfferedListingId, newCashAmount,
                newCashPayerId, counterMessage
            )
            result.onSuccess { newOfferId ->
                _userMessage.value = "Counter-offer sent! Terms updated while preserving history. ⚖️"
                onSuccess(newOfferId)
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to send counter-offer"
            }
        }
    }

    fun acceptOffer(offer: SwapOffer, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.acceptOffer(offer)
            result.onSuccess { transactionId ->
                _userMessage.value = "Swap offer accepted! Listings reserved. Coordinate handover in chat. 🤝"
                onSuccess(transactionId)
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to accept offer"
            }
        }
    }

    fun declineOffer(offerId: String) {
        viewModelScope.launch {
            val result = repository.declineOffer(offerId)
            result.onSuccess {
                _userMessage.value = "Offer declined."
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to decline offer"
            }
        }
    }

    fun withdrawOffer(offerId: String) {
        viewModelScope.launch {
            val result = repository.withdrawOffer(offerId)
            result.onSuccess {
                _userMessage.value = "Offer withdrawn."
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to withdraw offer"
            }
        }
    }

    // Transaction & Handover Dual Confirmation
    fun confirmHandover(transactionId: String) {
        viewModelScope.launch {
            val result = repository.confirmHandover(transactionId)
            result.onSuccess { bothConfirmed ->
                if (bothConfirmed) {
                    _userMessage.value = "Both parties confirmed! Swap completed! Please leave a review. 🌟"
                } else {
                    _userMessage.value = "Handover confirmed! Waiting for other party's confirmation."
                }
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to confirm handover"
            }
        }
    }

    fun fileDispute(transactionId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.fileDispute(transactionId, reason)
            result.onSuccess {
                _userMessage.value = "Dispute filed. SwapIt moderation team will investigate. ⚠️"
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to file dispute"
            }
        }
    }

    // Chat
    fun openOrCreateChat(otherUserId: String, listingId: String? = null, offerId: String? = null, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val convId = repository.getOrCreateConversation(otherUserId, listingId, offerId)
            onReady(convId)
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            val result = repository.sendMessage(conversationId, content)
            result.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to send message"
            }
        }
    }

    // Notifications
    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(currentUserId.value)
            _userMessage.value = "All notifications marked as read"
        }
    }

    // Review Submission
    fun submitReview(transactionId: String, revieweeId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val result = repository.submitReview(transactionId, revieweeId, rating, comment)
            result.onSuccess {
                _userMessage.value = "Review submitted! Thank you for building trust in SwapIt. ⭐"
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Failed to submit review"
            }
        }
    }

    // Trust & Safety
    fun submitReport(targetType: String, targetId: String, reason: String, description: String) {
        viewModelScope.launch {
            val result = repository.submitReport(targetType, targetId, reason, description)
            result.onSuccess {
                _userMessage.value = "Report received. Our moderation team reviews every submission promptly. 🛡️"
            }
        }
    }

    // Admin Operations
    fun updateReportStatus(reportId: String, status: String, adminNotes: String) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status, adminNotes)
            _userMessage.value = "Report marked as $status"
        }
    }

    fun suspendUser(targetUserId: String, reason: String) {
        viewModelScope.launch {
            repository.suspendUser(targetUserId, reason)
            _userMessage.value = "User account suspended."
        }
    }

    fun reactivateUser(targetUserId: String, reason: String) {
        viewModelScope.launch {
            repository.reactivateUser(targetUserId, reason)
            _userMessage.value = "User account reactivated."
        }
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedCategoryId.value = null
        selectedCondition.value = null
        selectedState.value = null
        selectedSort.value = SortOption.RECOMMENDED
        cashTopupOnly.value = false
    }
}
