package com.stride.cashflow.features.manage_items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stride.cashflow.data.ItemTemplate

// The fixed categories from your plan
val categories = listOf("Income", "Receivables", "Loans & EMI", "Credit Cards", "Personal Debt")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageItemsScreen(viewModel: ManageItemsViewModel) {
    val templates by viewModel.allTemplates.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Your Items") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            categories.forEach { category ->
                item {
                    CategorySection(
                        category = category,
                        items = templates.filter { it.category == category },
                        onAddItem = { itemName ->
                            viewModel.addItem(itemName, category)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySection(
    category: String,
    items: List<ItemTemplate>,
    onAddItem: (String) -> Unit
) {
    var newItemName by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = category, style = MaterialTheme.typography.titleLarge)

        // List of existing items in this category
        items.forEach { item ->
            Text(text = "- ${item.name}", modifier = Modifier.padding(start = 16.dp))
        }

        // Input field to add a new item
        OutlinedTextField(
            value = newItemName,
            onValueChange = { newItemName = it },
            label = { Text("Add new item...") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Button(onClick = {
                    onAddItem(newItemName)
                    newItemName = "" // Clear the field after adding
                }) {
                    Text("Add")
                }
            }
        )
    }
}
