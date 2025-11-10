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

    suspend fun createPlannerForMonth(month: String) {
        // Get all existing templates
        val templates = strideDao.getAllItemTemplates().first() // .first() gets the current list once

        // Create a new list of PlannerEntry objects, one for each template,
        // with default values for the given month.
        val newEntries = templates.map { template ->
            PlannerEntry(
                plannerMonth = month,
                templateId = template.id,
                amount = 0.0,
                isDone = false
            )
        }

        // Insert all these new entries into the database
        if (newEntries.isNotEmpty()) {
            strideDao.insertAllEntries(newEntries)
        }
    }

    // We need this function to check if a planner for a month already exists.
    fun getPlannerMonths(): Flow<List<String>> {
        return strideDao.getPlannerMonths()
    }
}
