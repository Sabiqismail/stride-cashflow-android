package com.stride.cashflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ItemTemplate::class, PlannerEntry::class],
    version = 3, // <-- 1. VERSION INCREASED TO 3
    exportSchema = false
)
abstract class StrideDatabase : RoomDatabase() {

    abstract fun strideDao(): StrideDao

    companion object {

        @Volatile
        private var INSTANCE: StrideDatabase? = null

        // 2. DEFINE THE MIGRATION FROM VERSION 2 to 3
        // Since there were no changes to the actual table structure between v2 and v3,
        // the body of this migration is empty. Its only job is to tell Room
        // that this transition is valid and that it shouldn't delete the data.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes, so the migration is empty.
            }
        }


        fun getDatabase(context: Context): StrideDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StrideDatabase::class.java,
                    "stride_database"
                )
                    // 3. REMOVE fallbackToDestructiveMigration
                    // .fallbackToDestructiveMigration() // <-- THIS IS REMOVED/COMMENTED OUT

                    // 4. ADD THE NEW MIGRATION
                    .addMigrations(MIGRATION_2_3) // <-- THIS IS ADDED
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
