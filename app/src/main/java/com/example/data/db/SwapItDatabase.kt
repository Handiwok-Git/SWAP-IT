package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        Category::class,
        ListingItem::class,
        Favourite::class,
        SwapOffer::class,
        SwapTransaction::class,
        Conversation::class,
        ChatMessage::class,
        NotificationItem::class,
        Review::class,
        Report::class,
        AdminAuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SwapItDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun listingDao(): ListingDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun offerDao(): OfferDao
    abstract fun transactionDao(): TransactionDao
    abstract fun chatDao(): ChatDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reviewDao(): ReviewDao
    abstract fun reportDao(): ReportDao
    abstract fun adminAuditDao(): AdminAuditDao

    companion object {
        @Volatile
        private var INSTANCE: SwapItDatabase? = null

        fun getDatabase(context: Context): SwapItDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SwapItDatabase::class.java,
                    "swapit_marketplace.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(database)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance

                // Double check seed population on startup in case table exists but is empty
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabaseIfEmpty(instance)
                }

                instance
            }
        }

        suspend fun seedDatabaseIfEmpty(db: SwapItDatabase) {
            val user = db.userDao().getUserById("user_emeka")
            if (user == null) {
                populateInitialData(db)
            }
        }

        private suspend fun populateInitialData(db: SwapItDatabase) {
            db.userDao().insertUsers(DatabaseSeedData.users)
            db.categoryDao().insertCategories(DatabaseSeedData.categories)
            db.listingDao().insertListings(DatabaseSeedData.listings)
            DatabaseSeedData.swapOffers.forEach { db.offerDao().insertOffer(it) }
            DatabaseSeedData.swapTransactions.forEach { db.transactionDao().insertTransaction(it) }
            DatabaseSeedData.conversations.forEach { db.chatDao().insertConversation(it) }
            DatabaseSeedData.chatMessages.forEach { db.chatDao().insertMessage(it) }
            DatabaseSeedData.notifications.forEach { db.notificationDao().insertNotification(it) }
            DatabaseSeedData.reviews.forEach { db.reviewDao().insertReview(it) }
        }
    }
}
