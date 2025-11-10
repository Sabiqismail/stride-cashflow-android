package com.stride.cashflow.features.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stride.cashflow.features.manage_items.categories
import com.stride.cashflow.ui.theme.SageGreen
import com.stride.cashflow.ui.theme.Terracotta
import com.stride.cashflow.utils.formatMonthString
import java.text.NumberFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    isCreateMode: Boolean, // <-- NEW PARAMETER
    month: String,
    viewModel: PlannerViewModel,
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit // <-- NEW PARAMETER
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCreateMode) "Create Planner" else formatMonthString(month)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // --- NEW DYNAMIC NAVIGATION AND ACTIONS ---
                navigationIcon = {
                    if (!isCreateMode) { // Only show back arrow in EDIT mode
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                        }
                    } else { // Show a "Cancel" button in CREATE mode
                        TextButton(onClick = onNavigateBack) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                actions = {
                    if (isCreateMode) { // Only show "Save" button in CREATE mode
                        Button(
                            onClick = {
                                viewModel.saveNewPlanner() // Call the save function
                                onSaveComplete()       // Navigate back
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                        ) {
                            Text("Save")
                        }
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
                // Add top padding to the list
                item { Spacer(modifier = Modifier.height(16.dp)) }

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
                // Add bottom padding to the list
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}


@Composable
fun TotalsHeader(inflows: Long, outflows: Long, netBalance: Long) {
    val numberFormat = NumberFormat.getNumberInstance()
    numberFormat.maximumFractionDigits = 0

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
            TotalColumn("Inflows", numberFormat.format(inflows), SageGreen)
            TotalColumn("Outflows", numberFormat.format(outflows), Terracotta)
            TotalColumn(
                "Net",
                numberFormat.format(netBalance),
                if (netBalance >= 0) SageGreen else Terracotta
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
    onAmountChange: (Int, Long) -> Unit,
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
    onAmountChange: (Int, Long) -> Unit,
    onDoneToggle: (UiPlannerItem) -> Unit
) {
    var textValue by remember(item.amount) { mutableStateOf(if (item.amount == 0L) "" else item.amount.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.isDone) 0.6f else 1.0f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Add some space between elements
    ) {
        // --- The Name and Checkbox are now grouped together ---
        Row(modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = item.isDone, onCheckedChange = { onDoneToggle(item) })
            Text(text = item.name)
        }

        // --- THE NEW, CUSTOM TEXT FIELD ---
        BasicTextField(
            value = textValue,
            onValueChange = { newValue ->
                // Allow up to 8 digits
                if (newValue.length <= 8) {
                    textValue = newValue
                    onAmountChange(item.templateId, newValue.toLongOrNull() ?: 0L)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = TextStyle( // Center the text inside the input field
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .width(140.dp) // Widen to comfortably fit 8 digits
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
