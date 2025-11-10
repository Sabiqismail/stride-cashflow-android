package com.stride.cashflow.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stride.cashflow.data.StrideRepository
import com.stride.cashflow.features.planner.INFLOW_CATEGORIES
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(private val  repository: StrideRepository) : ViewModel() {

    // The incorrect import for 'filter' has been removed.
    // This will now use the standard Kotlin 'filter' for lists.

    val dashboardState: StateFlow<List<DashboardPlannerCardData>> = combine(
        repository.getPlannerMonths(),
        repository.getAllEntries(),
        repository.getAllTemplates()
    ) { months, entries, templates ->
        // The 'map' function will now correctly infer its types
        months.map { month ->
            val entriesForMonth = entries.filter { it.plannerMonth == month }

            val totalInflows =  entriesForMonth
                .filter { entry -> templates.find { it.id == entry.templateId }?.category in INFLOW_CATEGORIES }
                .sumOf { it.amount }

            val totalOutflows = entriesForMonth
                .filterNot { entry -> templates.find { it.id == entry.templateId }?.category in INFLOW_CATEGORIES }
                .sumOf { it.amount }

            DashboardPlannerCardData(                monthString = month,
                netBalance = totalInflows - totalOutflows
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

// The factory remains the same
class DashboardViewModelFactory(private val repository: StrideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

