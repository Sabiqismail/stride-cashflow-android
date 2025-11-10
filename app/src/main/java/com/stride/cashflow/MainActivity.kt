package com.stride.cashflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stride.cashflow.features.dashboard.DashboardScreen
import com.stride.cashflow.features.dashboard.DashboardViewModel
import com.stride.cashflow.features.manage_items.ManageItemsScreen
import com.stride.cashflow.features.manage_items.ManageItemsViewModel
import com.stride.cashflow.features.planner.PlannerScreen
import com.stride.cashflow.features.planner.PlannerViewModel
import com.stride.cashflow.ui.theme.StrideCashflowTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

// --- NEW UNIFIED VIEWMODEL FACTORY ---
// This single factory can create ALL our ViewModels
object AppViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        // Get the repository from the application context via extras
        val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        val repository = (application as StrideApplication).repository

        // Get the SavedStateHandle, which is needed for PlannerViewModel
        val savedStateHandle = extras.createSavedStateHandle()

        // Decide which ViewModel to create based on the class requested
        return when (modelClass) {
            ManageItemsViewModel::class.java -> ManageItemsViewModel(repository) as T
            DashboardViewModel::class.java -> DashboardViewModel(repository) as T
            PlannerViewModel::class.java -> PlannerViewModel(repository, savedStateHandle) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            StrideCashflowTheme {
                // We pass our single, unified factory to the app
                StrideApp(factory = AppViewModelFactory)
            }
        }
    }
}

@Composable
fun StrideApp(factory: ViewModelProvider.Factory) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {

        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToManageItems = { navController.navigate("manage_items") },
                onNavigateToPlanner = { route -> // <-- This "route" is now "planner/create/2025-11" or "planner/2025-11"
                    navController.navigate(route)
                }
            )
        }

        composable("manage_items") {
            val manageItemsViewModel: ManageItemsViewModel = viewModel(factory = factory)
            ManageItemsScreen(viewModel = manageItemsViewModel)
        }

        // The "Edit Planner" route
        composable(
            route = "planner/{month}",
            arguments = listOf(navArgument("month") { type = NavType.StringType })
        ) {
            val plannerViewModel: PlannerViewModel = viewModel(factory = factory)
            val month = it.arguments?.getString("month") ?: "Error"
            PlannerScreen(
                isCreateMode = false, // This is EDIT mode
                month = month,
                viewModel = plannerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveComplete = {} // Not used in edit mode
            )
        }

        // --- THE NEW "CREATE PLANNER" ROUTE ---
        composable(
            route = "planner/create/{month}", // New route for creating
            arguments = listOf(navArgument("month") { type = NavType.StringType })
        ) {
            val plannerViewModel: PlannerViewModel = viewModel(factory = factory)
            val month = it.arguments?.getString("month") ?: "Error"
            PlannerScreen(
                isCreateMode = true, // This is CREATE mode
                month = month,
                viewModel = plannerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveComplete = {
                    // After saving, pop back to the dashboard
                    navController.popBackStack()
                }
            )
        }
    }
}
