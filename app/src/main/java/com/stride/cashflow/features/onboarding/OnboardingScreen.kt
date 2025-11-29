package com.stride.cashflow.features.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stride.cashflow.AppViewModelFactory // We'll need our factory

// The main entry point for the onboarding flow
@Composable
fun OnboardingFlow(onOnboardingComplete: () -> Unit) {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = viewModel(factory = AppViewModelFactory)

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("income") {
            OnboardingIncomeScreen(navController = navController, viewModel = onboardingViewModel)
        }
        composable("expenses") {
            OnboardingExpensesScreen(viewModel = onboardingViewModel, onOnboardingComplete = onOnboardingComplete)
        }
    }
}

// Step 1: Welcome Screen
@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Stride!", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "In just two quick steps, we'll add your main income and a few key expenses. This will give you your first cash flow snapshot in under a minute.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("income") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Let's Go!")
        }
    }
}

// Step 2a: Add Income
@Composable
fun OnboardingIncomeScreen(navController: NavController, viewModel: OnboardingViewModel) {
    var incomeName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val onAdd = {
        viewModel.addFlow(incomeName, "Income")
        focusManager.clearFocus()
        navController.navigate("expenses")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("First, what's your main source of income?", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = incomeName,
            onValueChange = { incomeName = it },
            label = { Text("e.g., Salary, Freelance Work") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAdd() })
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onAdd,
            enabled = incomeName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add")
        }
    }
}

// Step 2b: Add Expenses
@Composable
fun OnboardingExpensesScreen(viewModel: OnboardingViewModel, onOnboardingComplete: () -> Unit) {
    var expenseName by remember { mutableStateOf("") }
    var addedExpenses by remember { mutableStateOf(listOf<String>()) }
    val focusManager = LocalFocusManager.current

    val onAdd = {
        viewModel.addFlow(expenseName, "Fixed Expenses") // Defaulting to Fixed Expenses
        addedExpenses = addedExpenses + expenseName
        expenseName = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Great! Now, let's add a couple of major expenses.", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        // Simple list of added expenses
        addedExpenses.forEach {
            Text("• $it", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = expenseName,
            onValueChange = { expenseName = it },
            label = { Text("e.g., Rent, Car Payment") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onAdd() })
        )
        Spacer(Modifier.height(16.dp))

        // "Add More" button appears if the field is not blank
        if (expenseName.isNotBlank()) {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Text("Add More")
            }
        }

        // "Done" button appears after they've added at least one expense
        if (addedExpenses.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onOnboardingComplete, modifier = Modifier.fillMaxWidth()) {
                Text("Done, Continue to Planner")
            }
        }
    }
}
