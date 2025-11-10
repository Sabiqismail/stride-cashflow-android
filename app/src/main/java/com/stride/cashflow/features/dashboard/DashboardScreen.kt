package com.stride.cashflow.features.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Month
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToManageItems: () -> Unit,
    onNavigateToPlanner: (String) -> Unit
) {
    val plannerMonths by viewModel.plannerMonths.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Stride") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToManageItems) {
                        Icon(Icons.Filled.Settings, contentDescription = "Manage Items")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, // Show the dialog on click
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Planner")
            }
        }
    ) { paddingValues ->
        if (plannerMonths.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    text = "Create your first monthly planner by tapping the '+' button.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(plannerMonths) { month ->
                    PlannerMonthCard(month = month, onClick = { onNavigateToPlanner(month) })
                }
            }
        }
    }

    // Show the dialog when `showDialog` is true
    if (showDialog) {
        MonthYearPickerDialog(
            onDismiss = { showDialog = false },
            onConfirm = { month, year ->
                val monthString = "%d-%02d".format(year, month)
                viewModel.createNewPlanner(monthString)
                onNavigateToPlanner(monthString)
                showDialog = false
            }
        )
    }
}

@Composable
fun PlannerMonthCard(month: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Text(
            text = month,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerDialog(onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val currentYear = YearMonth.now().year
    val currentMonth = YearMonth.now().monthValue

    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }

    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }

    val years = (currentYear - 5..currentYear + 5).toList()
    val months = Month.values().map { it.value to it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Planner") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month Dropdown
                ExposedDropdownMenuBox(expanded = monthExpanded, onExpandedChange = { monthExpanded = !monthExpanded }) {
                    TextField(
                        readOnly = true,
                        value = months.first { it.first == selectedMonth }.second,
                        onValueChange = {},
                        label = { Text("Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                        months.forEach { (monthValue, monthName) ->
                            DropdownMenuItem(
                                text = { Text(monthName) },
                                onClick = {
                                    selectedMonth = monthValue
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }

                // Year Dropdown
                ExposedDropdownMenuBox(expanded = yearExpanded, onExpandedChange = { yearExpanded = !yearExpanded }) {
                    TextField(
                        readOnly = true,
                        value = selectedYear.toString(),
                        onValueChange = {},
                        label = { Text("Year") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMonth, selectedYear) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
