package com.stride.cashflow.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stride.cashflow.data.StrideRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: StrideRepository) : ViewModel() {

    // Get a list of all months that have planners, e.g., ["2025-11", "2025-10"]
    val plannerMonths: StateFlow<List<String>> = repository.getPlannerMonths()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createNewPlanner(month: String) {
        viewModelScope.launch {
            repository.createPlannerForMonth(month)
        }
    }
}

// Factory to create the ViewModel
class DashboardViewModelFactory(private val repository: StrideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
