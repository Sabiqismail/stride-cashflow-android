package com.stride.cashflow.features.manage_items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
// --- ACTION 1: Change the import ---
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.filled.Delete// Use the outlined version for the hyphen
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stride.cashflow.data.ItemTemplate
import com.stride.cashflow.ui.theme.SageGreen
import com.stride.cashflow.ui.theme.Terracotta

val categories = listOf("Income", "Receivables", "Fixed Expenses", "Variable Expenses", "Loans & EMI", "Credit Cards", "Personal Debt")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageItemsScreen(viewModel: ManageItemsViewModel) {
    val templates by viewModel.allTemplates.collectAsState()
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
                .padding(paddingValues)
        ) {
            categories.forEach { category ->
                val flows = templates.filter { it.category == category }

                item {
                    CategoryHeader(
                        categoryName = category,
                        onAddClick = { showDialogForCategory = category }
                    )
                }

                items(flows, key = { it.id }) { flow ->
                    FlowRow(
                        flowName = flow.name,
                        onDeleteClick = { viewModel.deleteFlow(flow.id) }
                    )
                }

                if (flows.isEmpty()) {
                    item {
                        Text(
                            text = "No flows added yet. Tap '+' to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            }
        }

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
fun CategoryHeader(categoryName: String, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Outlined.Add, // Using the outlined '+'
                contentDescription = "Add new flow to $categoryName",
                tint = SageGreen
            )
        }
    }
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
}

@Composable
fun FlowRow(flowName: String, onDeleteClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = flowName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    // --- ACTION 2: Change the imageVector ---
                    imageVector = Icons.Outlined.Delete, // Use the modern, outlined hyphen
                    contentDescription = "Delete $flowName",
                    tint = Terracotta
                )
            }
        }
        Divider(modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    }
}

// The AddFlowDialog remains unchanged
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
            Button(onClick = { if (flowName.isNotBlank()) onConfirm(flowName) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
