package com.isankamil.mcjobid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.isankamil.mcjobid.data.local.converter.Converters
import com.isankamil.mcjobid.data.local.dao.*
import com.isankamil.mcjobid.data.local.entity.*

@Database(
    entities = [
        BookingEntity::class,
        UserProfileEntity::class,
        ClientEntity::class,
        PaymentEntity::class,
        InvoiceEntity::class,
        ReminderEntity::class,
        ExpenseEntity::class,
        ChecklistEntity::class,
        SyncQueueEntity::class,
        RateCardEntity::class,
        TodoEntity::class
    ],
    version = 14,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class McJobIdDatabase : RoomDatabase() {
    
    abstract fun bookingDao(): BookingDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun clientDao(): ClientDao
    abstract fun paymentDao(): PaymentDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun reminderDao(): ReminderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun rateCardDao(): RateCardDao
    abstract fun todoDao(): TodoDao
    
    companion object {
        const val DATABASE_NAME = "mcjobid_database"

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Empty migration to trigger schema update if needed, 
                // but we rely on fallbackToDestructiveMigration for actual fixes
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bookings ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE clients ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE payments ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE invoices ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE checklists ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS sync_queue")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS job_templates")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_queue` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `ownerId` TEXT NOT NULL, 
                        `entityType` TEXT NOT NULL, 
                        `entityId` TEXT NOT NULL, 
                        `operation` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
