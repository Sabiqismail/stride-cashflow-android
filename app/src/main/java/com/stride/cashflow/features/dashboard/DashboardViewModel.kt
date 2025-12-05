package com.stride.cashflow.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stride.cashflow.data.StrideRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(private val repository: StrideRepository) : ViewModel() {

    // --- THE SIMPLIFIED AND CORRECT LOGIC ---
    // The dashboard's only job is to display a card for each month that has a planner.
    // We get the list of month strings (e.g., "2025-11") from the repository.
    // Then, we map that list into the `DashboardPlannerCardData` objects the UI expects.
    // Since we are not showing the net balance anymore, we can just set it to 0L.
    val dashboardState: StateFlow<List<DashboardPlannerCardData>> = repository.getPlannerMonths()
        .map { monthStrings ->
            monthStrings.map { month ->
                DashboardPlannerCardData(
                    monthString = month,
                    netBalance = 0L // We don't need the balance, so we can hardcode it.
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Start with an empty list while the database loads.
        )
}

class DashboardViewModelFactory(private val repository: StrideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
