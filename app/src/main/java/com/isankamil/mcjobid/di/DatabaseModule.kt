package com.isankamil.mcjobid.di

import android.content.Context
import androidx.room.Room
import com.isankamil.mcjobid.data.local.McJobIdDatabase
import com.isankamil.mcjobid.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): McJobIdDatabase {
        return Room.databaseBuilder(
            context,
            McJobIdDatabase::class.java,
            McJobIdDatabase.DATABASE_NAME
        ).addMigrations(
            McJobIdDatabase.MIGRATION_3_4,
            McJobIdDatabase.MIGRATION_4_5,
            McJobIdDatabase.MIGRATION_5_6,
            McJobIdDatabase.MIGRATION_6_7,
            McJobIdDatabase.MIGRATION_7_8,
            McJobIdDatabase.MIGRATION_8_9,
            McJobIdDatabase.MIGRATION_9_10,
            McJobIdDatabase.MIGRATION_11_12
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBookingDao(database: McJobIdDatabase): BookingDao {
        return database.bookingDao()
    }
    
    @Provides
    @Singleton
    fun provideUserProfileDao(database: McJobIdDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideClientDao(database: McJobIdDatabase): ClientDao {
        return database.clientDao()
    }

    @Provides
    @Singleton
    fun providePaymentDao(database: McJobIdDatabase): PaymentDao {
        return database.paymentDao()
    }

    @Provides
    @Singleton
    fun provideInvoiceDao(database: McJobIdDatabase): InvoiceDao {
        return database.invoiceDao()
    }

    @Provides
    @Singleton
    fun provideReminderDao(database: McJobIdDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: McJobIdDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideChecklistDao(database: McJobIdDatabase): ChecklistDao {
        return database.checklistDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(database: McJobIdDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }

    @Provides
    @Singleton
    fun provideRateCardDao(database: McJobIdDatabase): RateCardDao {
        return database.rateCardDao()
    }

    @Provides
    @Singleton
    fun provideTodoDao(database: McJobIdDatabase): TodoDao {
        return database.todoDao()
    }
}
