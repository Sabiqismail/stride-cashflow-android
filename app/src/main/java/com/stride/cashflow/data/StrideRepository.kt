package com.stride.cashflow.data
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow

class StrideRepository(private val strideDao: StrideDao) {

    // --- ItemTemplate Functions ---

    fun getAllTemplates(): Flow<List<ItemTemplate>> {
        return strideDao.getAllItemTemplates()
    }

    suspend fun insertItemTemplate(template: ItemTemplate) {
        strideDao.insertItemTemplate(template)
    }

    // --- NEW REPOSITORY FUNCTIONS FOR PLANNER ENTRIES ---

    fun getEntriesForMonth(month: String): Flow<List<PlannerEntry>> {
        return strideDao.getEntriesForMonth(month)
    }

    suspend fun upsertEntry(entry: PlannerEntry) {
        strideDao.upsertEntry(entry)
    }

    suspend fun createPlannerForMonth(entriesToSave: List<PlannerEntry>) {
        // Insert all the new entries that the user actually filled out.
        if (entriesToSave.isNotEmpty()) {
            strideDao.insertAllEntries(entriesToSave)
        }
    }

    // In data/StrideRepository.kt, add this function anywhere inside the class

    suspend fun deletePlannerForMonth(month: String) {
        strideDao.deleteEntriesForMonth(month)
    }


    fun getAllEntries(): Flow<List<PlannerEntry>> {
        return strideDao.getAllEntries()
    }

    // We need this function to check if a planner for a month already exists.
    fun getPlannerMonths(): Flow<List<String>> {
        return strideDao.getPlannerMonths()
    }
}
