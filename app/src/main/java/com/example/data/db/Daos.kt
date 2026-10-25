package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM profiles WHERE id = :userId")
    suspend fun getUserById(userId: String): UserProfile?

    @Query("SELECT * FROM profiles WHERE id = :userId")
    fun observeUserById(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM profiles")
    fun observeAllUsers(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserProfile>)

    @Update
    suspend fun updateUser(user: UserProfile)

    @Query("UPDATE profiles SET completedSwaps = completedSwaps + 1 WHERE id = :userId")
    suspend fun incrementCompletedSwaps(userId: String)

    @Query("UPDATE profiles SET accountStatus = :status WHERE id = :userId")
    suspend fun setAccountStatus(userId: String, status: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)
}

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings WHERE status = 'Active' ORDER BY createdAt DESC")
    fun observeActiveListings(): Flow<List<ListingItem>>

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getListingById(id: String): ListingItem?

    @Query("SELECT * FROM listings WHERE id = :id")
    fun observeListingById(id: String): Flow<ListingItem?>

    @Query("SELECT * FROM listings WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun observeListingsByOwner(ownerId: String): Flow<List<ListingItem>>

    @Query("SELECT * FROM listings WHERE ownerId = :ownerId AND status = :status ORDER BY createdAt DESC")
    fun observeListingsByOwnerAndStatus(ownerId: String, status: String): Flow<List<ListingItem>>

    @Query("SELECT * FROM listings WHERE status = 'Active' AND categoryId = :categoryId ORDER BY createdAt DESC")
    fun observeListingsByCategory(categoryId: String): Flow<List<ListingItem>>

    @Query("SELECT * FROM listings WHERE isFeatured = 1 AND status = 'Active' ORDER BY createdAt DESC LIMIT 10")
    fun observeFeaturedListings(): Flow<List<ListingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<ListingItem>)

    @Update
    suspend fun updateListing(listing: ListingItem)

    @Query("UPDATE listings SET status = :status WHERE id = :id")
    suspend fun updateListingStatus(id: String, status: String)

    @Delete
    suspend fun deleteListing(listing: ListingItem)

    @Query("SELECT COUNT(*) FROM listings WHERE status = 'Active'")
    fun observeActiveListingCount(): Flow<Int>
}

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites WHERE userId = :userId")
    fun observeFavourites(userId: String): Flow<List<Favourite>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE userId = :userId AND listingId = :listingId)")
    fun observeIsFavourite(userId: String, listingId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(favourite: Favourite)

    @Query("DELETE FROM favourites WHERE userId = :userId AND listingId = :listingId")
    suspend fun removeFavourite(userId: String, listingId: String)
}

@Dao
interface OfferDao {
    @Query("SELECT * FROM swap_offers WHERE senderId = :userId OR recipientId = :userId ORDER BY updatedAt DESC")
    fun observeUserOffers(userId: String): Flow<List<SwapOffer>>

    @Query("SELECT * FROM swap_offers WHERE id = :offerId")
    suspend fun getOfferById(offerId: String): SwapOffer?

    @Query("SELECT * FROM swap_offers WHERE id = :offerId")
    fun observeOfferById(offerId: String): Flow<SwapOffer?>

    @Query("SELECT * FROM swap_offers WHERE parentOfferId = :parentOfferId ORDER BY createdAt ASC")
    fun observeOfferHistory(parentOfferId: String): Flow<List<SwapOffer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: SwapOffer)

    @Update
    suspend fun updateOffer(offer: SwapOffer)

    @Query("UPDATE swap_offers SET status = :status, updatedAt = :updatedAt WHERE id = :offerId")
    suspend fun updateOfferStatus(offerId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM swap_offers WHERE status = 'Pending'")
    fun observePendingOfferCount(): Flow<Int>
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM swap_transactions WHERE participantOneId = :userId OR participantTwoId = :userId ORDER BY updatedAt DESC")
    fun observeUserTransactions(userId: String): Flow<List<SwapTransaction>>

    @Query("SELECT * FROM swap_transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): SwapTransaction?

    @Query("SELECT * FROM swap_transactions WHERE id = :id")
    fun observeTransactionById(id: String): Flow<SwapTransaction?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SwapTransaction)

    @Update
    suspend fun updateTransaction(transaction: SwapTransaction)

    @Query("SELECT COUNT(*) FROM swap_transactions WHERE status = 'Completed'")
    fun observeCompletedTransactionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM swap_transactions WHERE status = 'Disputed'")
    fun observeDisputeCount(): Flow<Int>
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations WHERE participantOneId = :userId OR participantTwoId = :userId ORDER BY lastMessageTimestamp DESC")
    fun observeConversationsForUser(userId: String): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE (participantOneId = :u1 AND participantTwoId = :u2) OR (participantOneId = :u2 AND participantTwoId = :u1) LIMIT 1")
    suspend fun findConversation(u1: String, u2: String): Conversation?

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: String): Conversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeNotifications(userId: String): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun observeUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE revieweeId = :userId ORDER BY createdAt DESC")
    fun observeReviewsForUser(userId: String): Flow<List<Review>>

    @Query("SELECT EXISTS(SELECT 1 FROM reviews WHERE transactionId = :transactionId AND reviewerId = :reviewerId)")
    suspend fun hasReviewed(transactionId: String, reviewerId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun observeAllReports(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("UPDATE reports SET status = :status, adminNotes = :adminNotes WHERE id = :id")
    suspend fun updateReportStatus(id: String, status: String, adminNotes: String?)

    @Query("SELECT COUNT(*) FROM reports WHERE status = 'Pending'")
    fun observePendingReportCount(): Flow<Int>
}

@Dao
interface AdminAuditDao {
    @Query("SELECT * FROM admin_audit_logs ORDER BY createdAt DESC")
    fun observeAuditLogs(): Flow<List<AdminAuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AdminAuditLog)
}
