package com.stride.cashflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ItemTemplate::class, PlannerEntry::class],version = 2,
    exportSchema = false // <-- ADD THIS LINE
)
abstract class StrideDatabase : RoomDatabase() {


    abstract fun strideDao(): StrideDao

    companion object {
        @Volatile
        private var INSTANCE: StrideDatabase? = null

        fun getDatabase(context: Context): StrideDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StrideDatabase::class.java,
                    "stride_database"
                )
                    .fallbackToDestructiveMigration() // For now, we will destroy and rebuild on schema changes
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
