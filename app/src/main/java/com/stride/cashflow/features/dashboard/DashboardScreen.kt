package com.stride.cashflow.features.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
// --- THE FIX IS HERE ---
// We are reverting to the 100% reliable, default 'Settings' (gear) icon to solve the build error.
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
import androidx.compose.ui.unit.sp
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
    onNavigateToPlanner: (String) -> Unit
) {
    val dashboardState by viewModel.dashboardState.collectAsState()

    Scaffold(
    ) { paddingValues ->
        if (dashboardState.isEmpty()) {
            EmptyDashboard(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dashboardState, key = { it.monthString }) { cardData ->
                    PlannerMonthCard(
                        cardData = cardData,
                        onClick = { onNavigateToPlanner("planner/${cardData.monthString}") }
                    )
                }
            }
        }
    }


}

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

@Composable
fun PlannerMonthCard(cardData: DashboardPlannerCardData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Text(
            text = formatMonthString(cardData.monthString),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MonthSliderDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val currentYearMonth = YearMonth.now()
    var displayedYearMonth by remember { mutableStateOf(currentYearMonth) }
    var slideDirection by remember { mutableStateOf(1) }
    val monthName = displayedYearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Planner", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayedYearMonth.year.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        slideDirection = -1
                        displayedYearMonth = displayedYearMonth.minusMonths(1)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }

                    AnimatedContent(
                        targetState = monthName,
                        transitionSpec = {
                            val enterTransition = if (slideDirection > 0)
                                slideInHorizontally { height -> height } + fadeIn()
                            else
                                slideInHorizontally { height -> -height } + fadeIn()

                            val exitTransition = if (slideDirection > 0)
                                slideOutHorizontally { height -> -height } + fadeOut()
                            else
                                slideOutHorizontally { height -> height } + fadeOut()

                            enterTransition togetherWith exitTransition
                        }
                    ) { targetMonth ->
                        Text(
                            text = targetMonth,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Light,
                            fontSize = 28.sp,
                            modifier = Modifier.width(140.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    IconButton(onClick = {
                        slideDirection = 1
                        displayedYearMonth = displayedYearMonth.plusMonths(1)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(displayedYearMonth.monthValue, displayedYearMonth.year) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            // No explicit dismiss button to keep the UI clean
        }
    )
}
