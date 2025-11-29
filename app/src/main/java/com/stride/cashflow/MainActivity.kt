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

// --- UNIFIED VIEWMODEL FACTORY ---
object AppViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        val repository = (application as StrideApplication).repository
        val savedStateHandle = extras.createSavedStateHandle()

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
                StrideApp(factory = AppViewModelFactory
                )
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
            DashboardScreen(viewModel = dashboardViewModel,
                onNavigateToManageItems = { navController.navigate("manage_items") },
                onNavigateToPlanner = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("manage_items") {
            val manageItemsViewModel: ManageItemsViewModel = viewModel(factory = factory)
            ManageItemsScreen(viewModel = manageItemsViewModel)
        }

        // --- THE "EDIT PLANNER" ROUTE ---
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
                onSaveComplete = { /* Not used in edit mode */ },
                onNavigateToEdit = { route -> navController.navigate(route) }
            )}

        // --- THE "CREATE PLANNER" ROUTE ---
        composable(
            route = "planner/create/{month}",
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
                },
                onNavigateToEdit = { /* Not used in create mode */ }
            )
        }
    }
}

