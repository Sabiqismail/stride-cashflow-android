package com.stride.cashflow.features.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stride.cashflow.features.manage_items.categories
import java.text.NumberFormat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draw.alpha
import com.stride.cashflow.ui.theme.SageGreen
import com.stride.cashflow.ui.theme.Terracotta


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    month: String,
    viewModel: PlannerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(month) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TotalsHeader(
                inflows = uiState.totalInflows,
                outflows = uiState.totalOutflows,
                netBalance = uiState.netBalance
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Group items by category for display
                val groupedItems = uiState.plannerItems.groupBy { it.category }

                categories.forEach { category ->
                    groupedItems[category]?.let { items ->
                        item {
                            CategorySection(
                                category = category,
                                items = items,
                                onAmountChange = { templateId, amount ->
                                    viewModel.updateAmount(templateId, amount)
                                },
                                onDoneToggle = { item ->
                                    viewModel.toggleDoneStatus(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TotalsHeader(inflows: Double, outflows: Double, netBalance: Double) {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    currencyFormat.maximumFractionDigits = 0 // For whole numbers
    currencyFormat.currency = java.util.Currency.getInstance("USD") // Placeholder

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TotalColumn("Inflows", currencyFormat.format(inflows), SageGreen) // Use SageGreen for positive
            TotalColumn("Outflows", currencyFormat.format(outflows), Terracotta) // Use Terracotta for negative
            TotalColumn(
                "Net",
                currencyFormat.format(netBalance),
                if (netBalance >= 0) SageGreen else Terracotta // Use brand colors here too
            )
        }
    }
}
@Composable
fun TotalColumn(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = color,
            fontSize = 18.sp
        )
    }
}


@Composable
fun CategorySection(
    category: String,
    items: List<UiPlannerItem>,
    onAmountChange: (Int, Double) -> Unit,
    onDoneToggle: (UiPlannerItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = category, style = MaterialTheme.typography.titleLarge)
        items.forEach { item ->
            ItemRow(item = item, onAmountChange = onAmountChange, onDoneToggle = onDoneToggle)
        }
    }
}

@Composable
fun ItemRow(
    item: UiPlannerItem,
    onAmountChange: (Int, Double) -> Unit,
    onDoneToggle: (UiPlannerItem) -> Unit
) {
    var textValue by remember(item.amount) { mutableStateOf(if (item.amount == 0.0) "" else item.amount.toString()) }

    // 2. Add the .alpha() modifier to this Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.isDone) 0.6f else 1.0f), // <-- THIS IS THE NEW LINE
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item.name, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                textValue = it
                onAmountChange(item.templateId, it.toDoubleOrNull() ?: 0.0)
            },
            modifier = Modifier.width(120.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("$") }
        )
        Checkbox(checked = item.isDone, onCheckedChange = { onDoneToggle(item) })
    }
}
