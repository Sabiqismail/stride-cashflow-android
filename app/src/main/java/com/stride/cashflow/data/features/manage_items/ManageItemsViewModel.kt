package com.stride.cashflow.features.manage_items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stride.cashflow.data.ItemTemplate
import com.stride.cashflow.data.StrideRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageItemsViewModel(private val repository: StrideRepository) : ViewModel() {

    // Get all templates from the repository and expose them as StateFlow.
    // The UI will observe this flow for updates.
    val allTemplates: StateFlow<List<ItemTemplate>> = repository.getAllTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addItem(name: String, category: String) {
        // Don't add if the name is blank
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.insertItemTemplate(ItemTemplate(name = name, category = category))
            }
        }
    }
}

// This is a "Factory" that tells our app how to create the ViewModel,
// because our ViewModel needs a Repository to be passed into its constructor.
class ManageItemsViewModelFactory(private val repository: StrideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageItemsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
