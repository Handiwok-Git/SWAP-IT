package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User Profile entity representing registered SwapIt members.
 */
@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val state: String,
    val city: String,
    val lga: String,
    val role: String = "user", // "user" or "admin"
    val accountStatus: String = "active", // "active" or "suspended"
    val completedSwaps: Int = 0,
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val memberSince: Long = System.currentTimeMillis(),
    val email: String = ""
)

/**
 * Marketplace Categories (15 categories per master prompt).
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val slug: String,
    val iconKey: String,
    val itemCount: Int = 0,
    val isActive: Boolean = true
)

/**
 * Item Listing entity.
 */
@Entity(tableName = "listings")
data class ListingItem(
    @PrimaryKey val id: String,
    val ownerId: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val brand: String,
    val model: String,
    val condition: String, // "Brand New", "Like New", "Excellent", "Good", "Fair", "For parts or repair"
    val estimatedValueNaira: Long, // in Naira ₦
    val isValueNegotiable: Boolean = true,
    val state: String,
    val city: String,
    val lga: String,
    val status: String = "Active", // "Active", "Pending Exchange", "Swapped", "Paused", "Archived"
    val acceptsCashTopup: Boolean = true,
    val willingToAddCash: Boolean = true,
    val willingToReceiveCash: Boolean = true,
    val itemForItemOnly: Boolean = false,
    val preferredCategories: String = "", // Comma-separated or description
    val preferredItems: String = "", // e.g. "iPhone 13, iPad Air, or Sony Camera"
    val handoverMethod: String = "In-person Public Meetup",
    val handoverAvailability: String = "Weekends and evenings",
    val primaryImageUrl: String = "",
    val additionalImageUrls: String = "",
    val isFeatured: Boolean = false,
    val viewsCount: Int = 0,
    val favouritesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * User Favourites / Saved Items.
 */
@Entity(tableName = "favourites")
data class Favourite(
    @PrimaryKey val id: String,
    val userId: String,
    val listingId: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Core Swap Offer entity representing a proposed exchange.
 */
@Entity(tableName = "swap_offers")
data class SwapOffer(
    @PrimaryKey val id: String,
    val senderId: String,
    val recipientId: String,
    val offeredListingId: String, // The item offered by sender
    val requestedListingId: String, // The item sender wants
    val cashAmount: Long = 0L, // Top-up amount in Naira
    val cashPayerId: String = "", // Who pays top-up: senderId, recipientId, or empty
    val currency: String = "NGN",
    val message: String = "",
    val status: String = "Pending", // "Pending", "Countered", "Accepted", "Declined", "Withdrawn", "Completed", "Cancelled"
    val parentOfferId: String? = null, // Set if this is a counter-offer
    val counterSummary: String? = null,
    val expiresAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 days expiry
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Swap Transaction representing an accepted offer undergoing handover and dual confirmation.
 */
@Entity(tableName = "swap_transactions")
data class SwapTransaction(
    @PrimaryKey val id: String,
    val acceptedOfferId: String,
    val participantOneId: String, // Offerer
    val participantTwoId: String, // Recipient
    val listingOneId: String,
    val listingTwoId: String,
    val cashAmount: Long = 0L,
    val cashPayerId: String = "",
    val status: String = "Awaiting Handover", // "Awaiting Handover", "Completed", "Disputed", "Cancelled"
    val participantOneConfirmed: Boolean = false,
    val participantTwoConfirmed: Boolean = false,
    val handoverNotes: String = "",
    val disputeReason: String? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Chat conversation between two users regarding listings/swaps.
 */
@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val participantOneId: String,
    val participantTwoId: String,
    val listingId: String? = null,
    val swapOfferId: String? = null,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCountUserOne: Int = 0,
    val unreadCountUserTwo: Int = 0
)

/**
 * Message within a conversation.
 */
@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

/**
 * In-App Notification.
 */
@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // "OFFER_RECEIVED", "OFFER_ACCEPTED", "OFFER_DECLINED", "COUNTER_OFFER", "MESSAGE", "CONFIRMATION_NEEDED", "SWAP_COMPLETED", "DISPUTE"
    val title: String,
    val body: String,
    val referenceType: String, // "offer", "transaction", "listing", "message"
    val referenceId: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * User Review following a completed swap transaction.
 */
@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String,
    val transactionId: String,
    val reviewerId: String,
    val revieweeId: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Safety & Trust Report.
 */
@Entity(tableName = "reports")
data class Report(
    @PrimaryKey val id: String,
    val reporterId: String,
    val targetType: String, // "listing", "user", "transaction"
    val targetId: String,
    val reason: String,
    val description: String,
    val status: String = "Pending", // "Pending", "Reviewed", "Resolved", "Dismissed"
    val adminNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Admin Audit Log.
 */
@Entity(tableName = "admin_audit_logs")
data class AdminAuditLog(
    @PrimaryKey val id: String,
    val adminId: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)
