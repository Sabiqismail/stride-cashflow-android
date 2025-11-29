package com.stride.cashflow.features.planner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stride.cashflow.data.PlannerEntry
import com.stride.cashflow.data.StrideRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val INFLOW_CATEGORIES = listOf("Income", "Receivables")

class PlannerViewModel(
    private val repository: StrideRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val month: String = savedStateHandle.get<String>("month")!!
    // Get the 'isCreateMode' flag passed from the navigation in MainActivity
    private val isCreateEditMode: Boolean = savedStateHandle.get<String>("isCreate")?.toBoolean() ?: false

    val uiState: StateFlow<PlannerUiState> = combine(
        repository.getAllTemplates(),
        repository.getEntriesForMonth(month)
    ) { templates, entriesFromDb ->

        if (templates.isEmpty()) {
            return@combine PlannerUiState(isLoading = true)
        }

        val uiPlannerItems: List<UiPlannerItem>

        // --- THE FINAL, CORRECT LOGIC ---
        if (isCreateEditMode) {
            // If in CREATE or EDIT mode, always show ALL templates, pre-filled with any saved data.
            uiPlannerItems = templates.map { template ->
                val savedEntry = entriesFromDb.find { it.templateId == template.id }
                UiPlannerItem(
                    entryId = savedEntry?.id ?: 0,
                    templateId = template.id,
                    name = template.name,
                    category = template.category,
                    amount = savedEntry?.amount ?: 0L,
                    isDone = savedEntry?.isDone ?: false
                )
            }
        } else {
            // If in VIEW mode (just viewing a saved planner), show ONLY items with saved entries.
            uiPlannerItems = entriesFromDb.mapNotNull { entry ->
                templates.find { it.id == entry.templateId }?.let { template ->
                    UiPlannerItem(
                        entryId = entry.id,
                        templateId = template.id,
                        name = template.name,
                        category = template.category,
                        amount = entry.amount,
                        isDone = entry.isDone
                    )
                }
            }
        }

        val totalInflows = uiPlannerItems
            .filter { it.category in INFLOW_CATEGORIES }
            .sumOf { it.amount }

        val totalOutflows = uiPlannerItems.filterNot { it.category in INFLOW_CATEGORIES }
            .sumOf { it.amount }

        PlannerUiState(
            isLoading = false,
            plannerItems = uiPlannerItems,
            totalInflows = totalInflows,
            totalOutflows = totalOutflows,
            netBalance = totalInflows - totalOutflows
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState(isLoading = true)
    )

    // The rest of the functions are now correct and do not need to be changed.
    fun updateAmount(templateId: Int, newAmount: Long) {
        viewModelScope.launch {
            val currentItem = uiState.value.plannerItems.find { it.templateId == templateId }
            val entry = PlannerEntry(
                id = currentItem?.entryId ?: 0,
                plannerMonth = month,
                templateId = templateId,
                amount = newAmount,
                isDone = currentItem?.isDone ?: false
            )
            repository.upsertEntry(entry)
        }
    }

    fun toggleDoneStatus(item: UiPlannerItem) {
        viewModelScope.launch {
            val updatedEntry = PlannerEntry(
                id = item.entryId,
                plannerMonth = month,
                templateId = item.templateId,
                amount = item.amount,
                isDone = !item.isDone
            )
            repository.upsertEntry(updatedEntry)
        }
    }

    fun saveNewPlanner() {
        viewModelScope.launch {
            val itemsToSave = uiState.value.plannerItems
                .filter { it.amount > 0L }
                .map { uiItem ->
                    PlannerEntry(
                        id = uiItem.entryId,
                        plannerMonth = month,
                        templateId = uiItem.templateId,
                        amount = uiItem.amount,
                        isDone = uiItem.isDone
                    )
                }
            repository.deletePlannerForMonth(month)
            repository.createPlannerForMonth(itemsToSave)
        }
    }

    fun deletePlanner() {
        viewModelScope.launch {
            repository.deletePlannerForMonth(month)
        }
    }
}

data class PlannerUiState(
    val isLoading: Boolean = true,
    val plannerItems: List<UiPlannerItem> = emptyList(),
    val totalInflows: Long = 0L,
    val totalOutflows: Long = 0L,
    val netBalance: Long = 0L
)

class PlannerViewModelFactory(private val repository: StrideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        error("Cannot create an instance of $modelClass")
    }
}
