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

    // This local-only flow holds the state during "create" mode before saving.
    private val _localCreateState = MutableStateFlow<List<UiPlannerItem>>(emptyList())

    val uiState: StateFlow<PlannerUiState> = combine(
        repository.getAllTemplates(),
        repository.getEntriesForMonth(month),
        _localCreateState    ) { templates, entriesFromDb, localState ->

        // If templates haven't loaded, return a loading state.
        if (templates.isEmpty()) {
            return@combine PlannerUiState(isLoading = true)
        }

        val isCreateMode = entriesFromDb.isEmpty()
        val uiPlannerItems: List<UiPlannerItem>

        if (isCreateMode) {
            // --- IN CREATE MODE ---
            // If our local state is empty, initialize it from templates.
            if (localState.isEmpty()) {_localCreateState.value = templates.map { template ->
                UiPlannerItem(
                    entryId = 0,
                    templateId = template.id,
                    name = template.name,
                    category = template.category,
                    amount = 0L,
                    isDone = false
                )
            }
                // Return a temporary loading state while the local state is populated for the first time
                return@combine PlannerUiState(isLoading = true)
            }
            // Use the local state as the source of truth.
            uiPlannerItems = localState
        } else {
            // --- IN EDIT MODE ---
            // Show only saved items from the database.
            uiPlannerItems = entriesFromDb.mapNotNull { entry ->
                templates.find { it.id == entry.templateId }?.let { template ->
                    UiPlannerItem(                        entryId = entry.id,
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

        val totalOutflows = uiPlannerItems
            .filterNot { it.category in INFLOW_CATEGORIES }
            .sumOf { it.amount }

        PlannerUiState(
            isLoading = false,
            plannerItems = uiPlannerItems,
            totalInflows = totalInflows,
            totalOutflows = totalOutflows,netBalance = totalInflows - totalOutflows
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState(isLoading = true)
    )

    fun updateAmount(templateId: Int, newAmount: Long) {
        val isCreateMode = uiState.value.plannerItems.any { it.entryId == 0 }

        if (isCreateMode) {
            // In create mode, just update the local state. DO NOT write to DB.
            _localCreateState.update { currentList ->
                currentList.map { item ->
                    if (item.templateId == templateId) {
                        item.copy(amount = newAmount)
                    } else {
                        item
                    }
                }
            }
        } else {            // In edit mode, write to the database as before.
            viewModelScope.launch {
                val currentItem = uiState.value.plannerItems.find { it.templateId == templateId }
                val entry = PlannerEntry(
                    id = currentItem?.entryId ?:0,
                    plannerMonth = month,
                    templateId = templateId,
                    amount = newAmount,
                    isDone = currentItem?.isDone ?: false
                )
                repository.upsertEntry(entry)
            }
        }
    }    fun toggleDoneStatus(item: UiPlannerItem) {
        // This can only happen in edit mode, so the original logic is fine.
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
        // Now, we read from the local state to save the planner.
        viewModelScope.launch {
            val entriesToSave = _localCreateState.value
                .filter { it.amount > 0L }
                .map { uiItem ->
                    PlannerEntry(                        id = 0,
                        plannerMonth = month,
                        templateId = uiItem.templateId,
                        amount = uiItem.amount,
                        isDone = uiItem.isDone
                    )
                }
            repository.createPlannerForMonth(entriesToSave)
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

