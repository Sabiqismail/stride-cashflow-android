package com.stride.cashflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StrideDao {

    // --- Functions for Item Templates ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemTemplate(template: ItemTemplate)

    @Query("SELECT * FROM item_templates ORDER BY category, name ASC")
    fun getAllItemTemplates(): Flow<List<ItemTemplate>>

    // --- NEW FUNCTIONS FOR PLANNER ENTRIES ---

    // Get all entries for a specific month (e.g., "2025-11")
    @Query("SELECT * FROM planner_entries WHERE plannerMonth = :month")
    fun getEntriesForMonth(month: String): Flow<List<PlannerEntry>>

    // In StrideDao.kt, add this anywhere inside the interface
    @Query("SELECT DISTINCT plannerMonth FROM planner_entries ORDER BY plannerMonth DESC")
    fun getPlannerMonths(): Flow<List<String>>

    // Insert or Update a single entry.
    // 'Upsert' is a convenient function that inserts if the item is new,
    // or updates it if it already exists.
    @Upsert
    suspend fun upsertEntry(entry: PlannerEntry)

    // This function will be used to create a new planner. It inserts a list
    // of new, empty entries for a given month.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllEntries(entries: List<PlannerEntry>)
}
