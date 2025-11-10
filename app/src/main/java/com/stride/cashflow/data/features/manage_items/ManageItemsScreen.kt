package com.stride.cashflow.features.manage_items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stride.cashflow.data.ItemTemplate

// The fixed categories from your plan
val categories = listOf("Income", "Receivables", "Fixed Expenses", "Loans & EMI", "Credit Cards", "Personal Debt")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageItemsScreen(viewModel: ManageItemsViewModel) {
    val templates by viewModel.allTemplates.collectAsState()
    // State to control which category's dialog is shown, if any
    var showDialogForCategory by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Your Cashflows") },
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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories.forEach { category ->
                item {
                    CategoryCard(
                        category = category,
                        flows = templates.filter { it.category == category },
                        onAddClick = { showDialogForCategory = category }
                    )
                }
            }
        }

        // This will show the Add Flow Dialog when a category is selected
        showDialogForCategory?.let { category ->
            AddFlowDialog(
                categoryName = category,
                onDismiss = { showDialogForCategory = null },
                onConfirm = { flowName ->
                    viewModel.addItem(flowName, category)
                    showDialogForCategory = null
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: String,
    flows: List<ItemTemplate>,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            if (flows.isEmpty()) {
                Text(
                    text = "No flows added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                flows.forEach { flow ->
                    Text(text = flow.name, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Flow", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add New Flow")
            }
        }
    }
}

@Composable
fun AddFlowDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var flowName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to $categoryName") },
        text = {
            OutlinedTextField(
                value = flowName,
                onValueChange = { flowName = it },
                label = { Text("Flow Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (flowName.isNotBlank()) {
                        onConfirm(flowName)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
