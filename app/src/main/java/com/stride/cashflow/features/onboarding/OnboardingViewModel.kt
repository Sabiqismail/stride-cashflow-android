package com.stride.cashflow.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stride.cashflow.data.ItemTemplate
import com.stride.cashflow.data.StrideRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: StrideRepository) : ViewModel() {

    fun addFlow(name: String, category: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                val template = ItemTemplate(name = name, category = category)
                repository.insertItemTemplate(template)
            }
        }
    }
}
