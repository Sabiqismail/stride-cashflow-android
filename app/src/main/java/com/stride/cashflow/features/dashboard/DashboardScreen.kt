package com.stride.cashflow.features.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stride.cashflow.ui.theme.SageGreen
import com.stride.cashflow.ui.theme.Terracotta
import com.stride.cashflow.utils.formatMonthString
import java.text.NumberFormat
import java.time.Month
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToManageItems: () -> Unit,
    onNavigateToPlanner: (String) -> Unit
) {
    // We now observe the new dashboardState
    val dashboardState by viewModel.dashboardState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Planners") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToManageItems) {
                        Icon(Icons.Filled.Settings, contentDescription = "Setup Your Cashflows")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Planner")
            }
        }
    ) { paddingValues ->
        if (dashboardState.isEmpty()) {
            // Use the new, cleaner EmptyDashboard composable
            EmptyDashboard(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // The list now iterates over the new DashboardPlannerCardData
                items(dashboardState, key = { it.monthString }) { cardData ->
                    PlannerMonthCard(
                        cardData = cardData,
                        // Navigate to the specific planner for editing
                        onClick = { onNavigateToPlanner("planner/${cardData.monthString}") }
                    )
                }
            }
        }
    }

    if (showDialog) {
        MonthYearPickerDialog(
            onDismiss = { showDialog = false },
            onConfirm = { month, year ->
                val monthString = "%d-%02d".format(year, month)
                // Navigate to the create screen
                onNavigateToPlanner("planner/create/$monthString")
                showDialog = false
            }
        )
    }
}

// A new composable for the empty state to keep the main screen clean
@Composable
fun EmptyDashboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Stride",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the '+' button to create\nyour first monthly planner.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// The new, redesigned card that shows the Net Balance
@Composable
fun PlannerMonthCard(cardData: DashboardPlannerCardData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // The Row and second Text composable have been removed for a simpler look.
        Text(
            text = formatMonthString(cardData.monthString),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp), // Apply padding directly here
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}



// The MonthYearPickerDialog composable remains unchanged
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
                            DropdownMenuItem(text = { Text(monthName) }, onClick = {
                                selectedMonth = monthValue
                                monthExpanded = false
                            })
                        }
                    }
                }
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
                            DropdownMenuItem(text = { Text(year.toString()) }, onClick = {
                                selectedYear = year
                                yearExpanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selectedMonth, selectedYear) }) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
