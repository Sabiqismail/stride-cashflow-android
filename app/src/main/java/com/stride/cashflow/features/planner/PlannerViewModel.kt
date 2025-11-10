package com.stride.cashflow.features.planner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stride.cashflow.data.PlannerEntry
import com.stride.cashflow.data.StrideRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Define inflow categories as per your design
val INFLOW_CATEGORIES = listOf("Income", "Receivables")

class PlannerViewModel(
    private val repository: StrideRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get the month from the navigation arguments
    private val month: String = savedStateHandle.get<String>("month")!!

    // This is the main state for the UI. It combines templates and entries.
    val uiState: StateFlow<PlannerUiState> = combine(
        repository.getAllTemplates(),
        repository.getEntriesForMonth(month)
    ) { templates, entries ->
        val uiPlannerItems = templates.map { template ->
            // Find the corresponding entry for this template, or create a default one
            val entry = entries.find { it.templateId == template.id }
                ?: PlannerEntry(templateId = template.id, plannerMonth = month)

            UiPlannerItem(
                entryId = entry.id,
                templateId = template.id,
                name = template.name,
                category = template.category,
                amount = entry.amount,
                isDone = entry.isDone
            )
        }

        // Calculate totals
        val totalInflows = uiPlannerItems
            .filter { it.category in INFLOW_CATEGORIES }
            .sumOf { it.amount }

        val totalOutflows = uiPlannerItems
            .filterNot { it.category in INFLOW_CATEGORIES }
            .sumOf { it.amount }

        PlannerUiState(
            plannerItems = uiPlannerItems,
            totalInflows = totalInflows,
            totalOutflows = totalOutflows,
            netBalance = totalInflows - totalOutflows
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState() // Start with an empty state
    )

    fun updateAmount(templateId: Int, newAmount: Double) {
        viewModelScope.launch {
            val entry = PlannerEntry(
                id = uiState.value.plannerItems.find { it.templateId == templateId }?.entryId ?: 0,
                plannerMonth = month,
                templateId = templateId,
                amount = newAmount,
                isDone = uiState.value.plannerItems.find { it.templateId == templateId }?.isDone ?: false
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
                isDone = !item.isDone // Flip the status
            )
            repository.upsertEntry(updatedEntry)
        }
    }
}

// Data class to hold the entire screen state
data class PlannerUiState(
    val plannerItems: List<UiPlannerItem> = emptyList(),
    val totalInflows: Double = 0.0,
    val totalOutflows: Double = 0.0,
    val netBalance: Double = 0.0
)

// Factory for creating the PlannerViewModel
class PlannerViewModelFactory(private val repository: StrideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // This factory is a bit limited as it can't handle SavedStateHandle directly
        // For a full implementation, we'd use Hilt or a custom provider.
        // This is a simplification for our current setup.
        error("Cannot create an instance of $modelClass")
    }
}
