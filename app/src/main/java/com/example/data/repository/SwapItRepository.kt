package com.example.data.repository

import android.content.Context
import com.example.data.db.SwapItDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SwapItRepository(context: Context) {

    private val db = SwapItDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val listingDao = db.listingDao()
    private val favouriteDao = db.favouriteDao()
    private val offerDao = db.offerDao()
    private val transactionDao = db.transactionDao()
    private val chatDao = db.chatDao()
    private val notificationDao = db.notificationDao()
    private val reviewDao = db.reviewDao()
    private val reportDao = db.reportDao()
    private val adminAuditDao = db.adminAuditDao()

    // Active session user ID (Defaulting to Emeka Obi for immediate interactive use)
    private val _currentUserId = MutableStateFlow("user_emeka")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun switchActiveUser(userId: String) {
        _currentUserId.value = userId
    }

    suspend fun ensureSeeded() {
        SwapItDatabase.seedDatabaseIfEmpty(db)
    }

    // Profiles
    fun observeUser(userId: String): Flow<UserProfile?> = userDao.observeUserById(userId)
    suspend fun getUser(userId: String): UserProfile? = userDao.getUserById(userId)
    fun observeAllUsers(): Flow<List<UserProfile>> = userDao.observeAllUsers()

    suspend fun updateUser(user: UserProfile) {
        userDao.updateUser(user)
    }

    suspend fun registerUser(displayName: String, username: String, email: String, state: String, city: String, bio: String): String {
        val newId = "user_" + UUID.randomUUID().toString().take(8)
        val newUser = UserProfile(
            id = newId,
            username = username.lowercase().replace(" ", "_"),
            displayName = displayName,
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            bio = bio,
            state = state,
            city = city,
            lga = city,
            email = email,
            role = "user"
        )
        userDao.insertUser(newUser)
        _currentUserId.value = newId
        return newId
    }

    // Categories
    fun observeAllCategories(): Flow<List<Category>> = categoryDao.observeAllCategories()
    suspend fun getCategory(id: String): Category? = categoryDao.getCategoryById(id)

    // Listings
    fun observeActiveListings(): Flow<List<ListingItem>> = listingDao.observeActiveListings()
    fun observeFeaturedListings(): Flow<List<ListingItem>> = listingDao.observeFeaturedListings()
    fun observeListing(id: String): Flow<ListingItem?> = listingDao.observeListingById(id)
    suspend fun getListing(id: String): ListingItem? = listingDao.getListingById(id)
    fun observeListingsByOwner(ownerId: String): Flow<List<ListingItem>> = listingDao.observeListingsByOwner(ownerId)
    fun observeListingsByOwnerAndStatus(ownerId: String, status: String): Flow<List<ListingItem>> =
        listingDao.observeListingsByOwnerAndStatus(ownerId, status)
    fun observeListingsByCategory(categoryId: String): Flow<List<ListingItem>> = listingDao.observeListingsByCategory(categoryId)

    suspend fun createListing(
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
        primaryImageUrl: String
    ): String {
        val newId = "list_" + UUID.randomUUID().toString().take(8)
        val listing = ListingItem(
            id = newId,
            ownerId = _currentUserId.value,
            categoryId = categoryId,
            title = title,
            description = description,
            brand = brand,
            model = model,
            condition = condition,
            estimatedValueNaira = estimatedValueNaira,
            isValueNegotiable = isValueNegotiable,
            state = state,
            city = city,
            lga = lga,
            status = "Active",
            acceptsCashTopup = acceptsCashTopup,
            willingToAddCash = willingToAddCash,
            willingToReceiveCash = willingToReceiveCash,
            itemForItemOnly = itemForItemOnly,
            preferredCategories = preferredCategories,
            preferredItems = preferredItems,
            handoverMethod = handoverMethod,
            handoverAvailability = handoverAvailability,
            primaryImageUrl = primaryImageUrl.ifBlank { "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600" },
            createdAt = System.currentTimeMillis()
        )
        listingDao.insertListing(listing)
        return newId
    }

    suspend fun updateListing(listing: ListingItem) {
        listingDao.updateListing(listing)
    }

    suspend fun deleteListing(listing: ListingItem) {
        listingDao.deleteListing(listing)
    }

    suspend fun setListingStatus(id: String, status: String) {
        listingDao.updateListingStatus(id, status)
    }

    // Favourites
    fun observeFavourites(userId: String): Flow<List<Favourite>> = favouriteDao.observeFavourites(userId)
    fun observeIsFavourite(userId: String, listingId: String): Flow<Boolean> = favouriteDao.observeIsFavourite(userId, listingId)

    suspend fun toggleFavourite(userId: String, listingId: String, currentIsFav: Boolean) {
        if (currentIsFav) {
            favouriteDao.removeFavourite(userId, listingId)
        } else {
            favouriteDao.addFavourite(
                Favourite(
                    id = "fav_${userId}_$listingId",
                    userId = userId,
                    listingId = listingId
                )
            )
        }
    }

    // Offers & Swapping
    fun observeUserOffers(userId: String): Flow<List<SwapOffer>> = offerDao.observeUserOffers(userId)
    fun observeOffer(offerId: String): Flow<SwapOffer?> = offerDao.observeOfferById(offerId)
    suspend fun getOffer(offerId: String): SwapOffer? = offerDao.getOfferById(offerId)

    suspend fun sendSwapOffer(
        offeredListingId: String,
        requestedListingId: String,
        recipientId: String,
        cashAmount: Long,
        cashPayerId: String,
        message: String
    ): Result<String> {
        val senderId = _currentUserId.value
        if (senderId == recipientId) {
            return Result.failure(IllegalArgumentException("You cannot make a swap offer to yourself."))
        }
        val offeredItem = listingDao.getListingById(offeredListingId)
        if (offeredItem == null || offeredItem.ownerId != senderId) {
            return Result.failure(IllegalArgumentException("You can only offer items you own."))
        }
        if (offeredItem.status != "Active") {
            return Result.failure(IllegalArgumentException("The offered item is currently unavailable."))
        }
        val requestedItem = listingDao.getListingById(requestedListingId)
        if (requestedItem == null || requestedItem.status != "Active") {
            return Result.failure(IllegalArgumentException("The requested item is not available."))
        }

        val offerId = "offer_" + UUID.randomUUID().toString().take(8)
        val offer = SwapOffer(
            id = offerId,
            senderId = senderId,
            recipientId = recipientId,
            offeredListingId = offeredListingId,
            requestedListingId = requestedListingId,
            cashAmount = maxOf(0L, cashAmount),
            cashPayerId = if (cashAmount > 0) cashPayerId else "",
            message = message,
            status = "Pending"
        )
        offerDao.insertOffer(offer)

        // Create in-app notification for recipient
        val senderUser = userDao.getUserById(senderId)
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = recipientId,
                type = "OFFER_RECEIVED",
                title = "New Swap Offer! 🔄",
                body = "${senderUser?.displayName ?: "Someone"} proposed swapping '${offeredItem.title}' for your '${requestedItem.title}'.",
                referenceType = "offer",
                referenceId = offerId
            )
        )

        return Result.success(offerId)
    }

    suspend fun counterOffer(
        parentOfferId: String,
        newOfferedListingId: String,
        newCashAmount: Long,
        newCashPayerId: String,
        counterMessage: String
    ): Result<String> {
        val parent = offerDao.getOfferById(parentOfferId)
            ?: return Result.failure(IllegalArgumentException("Original offer not found"))

        val currentActor = _currentUserId.value
        val isRecipient = (currentActor == parent.recipientId)
        val isSender = (currentActor == parent.senderId)
        if (!isRecipient && !isSender) {
            return Result.failure(IllegalArgumentException("Unauthorized to counter this offer"))
        }

        // Mark parent offer as Countered
        offerDao.updateOfferStatus(parentOfferId, "Countered")

        // Invert sender/recipient for the counter-offer
        val counterSenderId = currentActor
        val counterRecipientId = if (isRecipient) parent.senderId else parent.recipientId

        val newOfferId = "offer_" + UUID.randomUUID().toString().take(8)
        val counterOffer = SwapOffer(
            id = newOfferId,
            senderId = counterSenderId,
            recipientId = counterRecipientId,
            offeredListingId = newOfferedListingId,
            requestedListingId = if (counterSenderId == parent.senderId) parent.requestedListingId else parent.offeredListingId,
            cashAmount = maxOf(0L, newCashAmount),
            cashPayerId = if (newCashAmount > 0) newCashPayerId else "",
            message = counterMessage,
            status = "Pending",
            parentOfferId = parentOfferId,
            counterSummary = "Counter-offer with ₦${newCashAmount} cash difference proposal"
        )
        offerDao.insertOffer(counterOffer)

        // Notify counterpart
        val actor = userDao.getUserById(currentActor)
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = counterRecipientId,
                type = "COUNTER_OFFER",
                title = "Counter-Offer Received ⚖️",
                body = "${actor?.displayName ?: "The other party"} sent a counter-offer on your swap proposal.",
                referenceType = "offer",
                referenceId = newOfferId
            )
        )

        return Result.success(newOfferId)
    }

    suspend fun acceptOffer(offer: SwapOffer): Result<String> {
        val currentActor = _currentUserId.value
        if (currentActor != offer.recipientId) {
            return Result.failure(IllegalArgumentException("Only the offer recipient can accept this offer."))
        }

        val offeredItem = listingDao.getListingById(offer.offeredListingId)
        val requestedItem = listingDao.getListingById(offer.requestedListingId)
        if (offeredItem == null || requestedItem == null) {
            return Result.failure(IllegalArgumentException("One of the listings no longer exists."))
        }

        if (offeredItem.status == "Pending Exchange" || requestedItem.status == "Pending Exchange" ||
            offeredItem.status == "Swapped" || requestedItem.status == "Swapped") {
            return Result.failure(IllegalArgumentException("One of the items is already reserved in another transaction."))
        }

        // Mark offer as Accepted
        offerDao.updateOfferStatus(offer.id, "Accepted")

        // Lock listings against competing swaps (Business Rule #10)
        listingDao.updateListingStatus(offeredItem.id, "Pending Exchange")
        listingDao.updateListingStatus(requestedItem.id, "Pending Exchange")

        // Create transaction
        val transactionId = "trans_" + UUID.randomUUID().toString().take(8)
        val transaction = SwapTransaction(
            id = transactionId,
            acceptedOfferId = offer.id,
            participantOneId = offer.senderId,
            participantTwoId = offer.recipientId,
            listingOneId = offer.offeredListingId,
            listingTwoId = offer.requestedListingId,
            cashAmount = offer.cashAmount,
            cashPayerId = offer.cashPayerId,
            status = "Awaiting Handover",
            handoverNotes = "Swap agreed! Please arrange a safe public handover meeting."
        )
        transactionDao.insertTransaction(transaction)

        // Notify both parties
        val recipientUser = userDao.getUserById(offer.recipientId)
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = offer.senderId,
                type = "OFFER_ACCEPTED",
                title = "Swap Offer Accepted! 🎉",
                body = "${recipientUser?.displayName ?: "The owner"} accepted your swap offer for '${requestedItem.title}'. Arrange the handover!",
                referenceType = "transaction",
                referenceId = transactionId
            )
        )
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = offer.recipientId,
                type = "OFFER_ACCEPTED",
                title = "Swap Transaction Created 🤝",
                body = "You accepted the swap for '${requestedItem.title}'. Coordinate with the trader for handover.",
                referenceType = "transaction",
                referenceId = transactionId
            )
        )

        return Result.success(transactionId)
    }

    suspend fun declineOffer(offerId: String): Result<Unit> {
        val offer = offerDao.getOfferById(offerId) ?: return Result.failure(IllegalArgumentException("Offer not found"))
        if (_currentUserId.value != offer.recipientId) {
            return Result.failure(IllegalArgumentException("Only recipient can decline this offer."))
        }
        offerDao.updateOfferStatus(offerId, "Declined")
        val recipient = userDao.getUserById(offer.recipientId)
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = offer.senderId,
                type = "OFFER_DECLINED",
                title = "Swap Offer Declined",
                body = "${recipient?.displayName ?: "The owner"} declined your swap proposal.",
                referenceType = "offer",
                referenceId = offerId
            )
        )
        return Result.success(Unit)
    }

    suspend fun withdrawOffer(offerId: String): Result<Unit> {
        val offer = offerDao.getOfferById(offerId) ?: return Result.failure(IllegalArgumentException("Offer not found"))
        if (_currentUserId.value != offer.senderId) {
            return Result.failure(IllegalArgumentException("Only sender can withdraw this offer."))
        }
        offerDao.updateOfferStatus(offerId, "Withdrawn")
        return Result.success(Unit)
    }

    // Transactions & Dual Confirmation
    fun observeUserTransactions(userId: String): Flow<List<SwapTransaction>> = transactionDao.observeUserTransactions(userId)
    fun observeTransaction(id: String): Flow<SwapTransaction?> = transactionDao.observeTransactionById(id)
    suspend fun getTransaction(id: String): SwapTransaction? = transactionDao.getTransactionById(id)

    suspend fun confirmHandover(transactionId: String): Result<Boolean> {
        val transaction = transactionDao.getTransactionById(transactionId)
            ?: return Result.failure(IllegalArgumentException("Transaction not found"))

        val currentActor = _currentUserId.value
        val isP1 = (currentActor == transaction.participantOneId)
        val isP2 = (currentActor == transaction.participantTwoId)
        if (!isP1 && !isP2) {
            return Result.failure(IllegalArgumentException("Unauthorized participant."))
        }

        val updatedP1 = if (isP1) true else transaction.participantOneConfirmed
        val updatedP2 = if (isP2) true else transaction.participantTwoConfirmed
        val bothConfirmed = updatedP1 && updatedP2

        val newStatus = if (bothConfirmed) "Completed" else "Awaiting Handover"
        val updatedTransaction = transaction.copy(
            participantOneConfirmed = updatedP1,
            participantTwoConfirmed = updatedP2,
            status = newStatus,
            completedAt = if (bothConfirmed) System.currentTimeMillis() else null,
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.updateTransaction(updatedTransaction)

        if (bothConfirmed) {
            // Mark listings as Swapped
            listingDao.updateListingStatus(transaction.listingOneId, "Swapped")
            listingDao.updateListingStatus(transaction.listingTwoId, "Swapped")

            // Increment completed swaps for both
            userDao.incrementCompletedSwaps(transaction.participantOneId)
            userDao.incrementCompletedSwaps(transaction.participantTwoId)

            // Notify both parties of completion & review prompt
            notificationDao.insertNotification(
                NotificationItem(
                    id = "notif_" + UUID.randomUUID().toString().take(8),
                    userId = transaction.participantOneId,
                    type = "SWAP_COMPLETED",
                    title = "Swap Completed! 🌟",
                    body = "Both parties confirmed the exchange. Please leave a review for your trading partner!",
                    referenceType = "transaction",
                    referenceId = transactionId
                )
            )
            notificationDao.insertNotification(
                NotificationItem(
                    id = "notif_" + UUID.randomUUID().toString().take(8),
                    userId = transaction.participantTwoId,
                    type = "SWAP_COMPLETED",
                    title = "Swap Completed! 🌟",
                    body = "Both parties confirmed the exchange. Please leave a review for your trading partner!",
                    referenceType = "transaction",
                    referenceId = transactionId
                )
            )
        } else {
            // Notify the counterpart that partner confirmed
            val otherUserId = if (isP1) transaction.participantTwoId else transaction.participantOneId
            val actor = userDao.getUserById(currentActor)
            notificationDao.insertNotification(
                NotificationItem(
                    id = "notif_" + UUID.randomUUID().toString().take(8),
                    userId = otherUserId,
                    type = "CONFIRMATION_NEEDED",
                    title = "Partner Confirmed Handover 🤝",
                    body = "${actor?.displayName ?: "Partner"} confirmed item handover. Please confirm once you inspect your item.",
                    referenceType = "transaction",
                    referenceId = transactionId
                )
            )
        }

        return Result.success(bothConfirmed)
    }

    suspend fun fileDispute(transactionId: String, reason: String): Result<Unit> {
        val transaction = transactionDao.getTransactionById(transactionId)
            ?: return Result.failure(IllegalArgumentException("Transaction not found"))
        val currentActor = _currentUserId.value
        if (currentActor != transaction.participantOneId && currentActor != transaction.participantTwoId) {
            return Result.failure(IllegalArgumentException("Unauthorized participant."))
        }

        val updated = transaction.copy(
            status = "Disputed",
            disputeReason = reason,
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.updateTransaction(updated)

        // Notify admin and counterpart
        val otherUserId = if (currentActor == transaction.participantOneId) transaction.participantTwoId else transaction.participantOneId
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = otherUserId,
                type = "DISPUTE",
                title = "Dispute Filed on Swap ⚠️",
                body = "A dispute was filed: '$reason'. The SwapIt moderation team has been alerted.",
                referenceType = "transaction",
                referenceId = transactionId
            )
        )
        reportDao.insertReport(
            Report(
                id = "rep_" + UUID.randomUUID().toString().take(8),
                reporterId = currentActor,
                targetType = "transaction",
                targetId = transactionId,
                reason = "Transaction Dispute",
                description = reason,
                status = "Pending"
            )
        )
        return Result.success(Unit)
    }

    // Chat & Conversations
    fun observeConversationsForUser(userId: String): Flow<List<Conversation>> = chatDao.observeConversationsForUser(userId)
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = chatDao.observeMessages(conversationId)

    suspend fun getOrCreateConversation(otherUserId: String, listingId: String? = null, offerId: String? = null): String {
        val current = _currentUserId.value
        val existing = chatDao.findConversation(current, otherUserId)
        if (existing != null) {
            return existing.id
        }
        val newId = "conv_" + UUID.randomUUID().toString().take(8)
        val conversation = Conversation(
            id = newId,
            participantOneId = current,
            participantTwoId = otherUserId,
            listingId = listingId,
            swapOfferId = offerId,
            lastMessageText = "Started a conversation",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        chatDao.insertConversation(conversation)
        return newId
    }

    suspend fun sendMessage(conversationId: String, content: String): Result<Unit> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("Message cannot be blank"))
        val senderId = _currentUserId.value
        val conv = chatDao.getConversationById(conversationId)
            ?: return Result.failure(IllegalArgumentException("Conversation not found"))

        val message = ChatMessage(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(message)

        // Update conversation summary
        val recipientId = if (conv.participantOneId == senderId) conv.participantTwoId else conv.participantOneId
        val updatedConv = conv.copy(
            lastMessageText = content,
            lastMessageTimestamp = System.currentTimeMillis()
        )
        chatDao.updateConversation(updatedConv)

        // In-app notification for recipient
        val senderUser = userDao.getUserById(senderId)
        notificationDao.insertNotification(
            NotificationItem(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = recipientId,
                type = "MESSAGE",
                title = "New Message from ${senderUser?.displayName ?: "Trader"}",
                body = content.take(60),
                referenceType = "message",
                referenceId = conversationId
            )
        )

        return Result.success(Unit)
    }

    // Notifications
    fun observeNotifications(userId: String): Flow<List<NotificationItem>> = notificationDao.observeNotifications(userId)
    fun observeUnreadNotificationCount(userId: String): Flow<Int> = notificationDao.observeUnreadCount(userId)

    suspend fun markNotificationRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }

    // Reviews
    fun observeReviewsForUser(userId: String): Flow<List<Review>> = reviewDao.observeReviewsForUser(userId)
    suspend fun hasReviewed(transactionId: String, reviewerId: String): Boolean = reviewDao.hasReviewed(transactionId, reviewerId)

    suspend fun submitReview(transactionId: String, revieweeId: String, rating: Int, comment: String): Result<Unit> {
        val reviewerId = _currentUserId.value
        if (hasReviewed(transactionId, reviewerId)) {
            return Result.failure(IllegalArgumentException("You have already reviewed this transaction."))
        }
        val review = Review(
            id = "rev_" + UUID.randomUUID().toString().take(8),
            transactionId = transactionId,
            reviewerId = reviewerId,
            revieweeId = revieweeId,
            rating = rating.coerceIn(1, 5),
            comment = comment
        )
        reviewDao.insertReview(review)

        // Recompute user rating
        val targetUser = userDao.getUserById(revieweeId)
        if (targetUser != null) {
            val totalScore = (targetUser.rating * targetUser.reviewCount) + rating
            val newCount = targetUser.reviewCount + 1
            val newAvg = (totalScore / newCount).coerceIn(1.0, 5.0)
            userDao.updateUser(targetUser.copy(rating = Math.round(newAvg * 10.0) / 10.0, reviewCount = newCount))
        }

        return Result.success(Unit)
    }

    // Trust, Safety & Reports
    suspend fun submitReport(targetType: String, targetId: String, reason: String, description: String): Result<Unit> {
        val reporterId = _currentUserId.value
        val report = Report(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            reporterId = reporterId,
            targetType = targetType,
            targetId = targetId,
            reason = reason,
            description = description,
            status = "Pending"
        )
        reportDao.insertReport(report)
        return Result.success(Unit)
    }

    fun observeAllReports(): Flow<List<Report>> = reportDao.observeAllReports()

    suspend fun updateReportStatus(reportId: String, status: String, adminNotes: String?): Result<Unit> {
        reportDao.updateReportStatus(reportId, status, adminNotes)
        adminAuditDao.insertAuditLog(
            AdminAuditLog(
                id = "audit_" + UUID.randomUUID().toString().take(8),
                adminId = _currentUserId.value,
                action = "UPDATE_REPORT_STATUS",
                targetType = "report",
                targetId = reportId,
                reason = "Status changed to $status: $adminNotes"
            )
        )
        return Result.success(Unit)
    }

    suspend fun suspendUser(targetUserId: String, reason: String): Result<Unit> {
        userDao.setAccountStatus(targetUserId, "suspended")
        adminAuditDao.insertAuditLog(
            AdminAuditLog(
                id = "audit_" + UUID.randomUUID().toString().take(8),
                adminId = _currentUserId.value,
                action = "SUSPEND_USER",
                targetType = "user",
                targetId = targetUserId,
                reason = reason
            )
        )
        return Result.success(Unit)
    }

    suspend fun reactivateUser(targetUserId: String, reason: String): Result<Unit> {
        userDao.setAccountStatus(targetUserId, "active")
        adminAuditDao.insertAuditLog(
            AdminAuditLog(
                id = "audit_" + UUID.randomUUID().toString().take(8),
                adminId = _currentUserId.value,
                action = "REACTIVATE_USER",
                targetType = "user",
                targetId = targetUserId,
                reason = reason
            )
        )
        return Result.success(Unit)
    }

    fun observeAuditLogs(): Flow<List<AdminAuditLog>> = adminAuditDao.observeAuditLogs()

    // Metrics for Admin Dashboard
    fun observeActiveListingCount(): Flow<Int> = listingDao.observeActiveListingCount()
    fun observePendingOfferCount(): Flow<Int> = offerDao.observePendingOfferCount()
    fun observeCompletedTransactionCount(): Flow<Int> = transactionDao.observeCompletedTransactionCount()
    fun observeDisputeCount(): Flow<Int> = transactionDao.observeDisputeCount()
    fun observePendingReportCount(): Flow<Int> = reportDao.observePendingReportCount()
}
