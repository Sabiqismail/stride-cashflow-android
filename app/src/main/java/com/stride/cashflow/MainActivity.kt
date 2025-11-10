package com.stride.cashflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.stride.cashflow.features.dashboard.DashboardViewModelFactory
import com.stride.cashflow.features.manage_items.ManageItemsScreen
import com.stride.cashflow.features.manage_items.ManageItemsViewModel
import com.stride.cashflow.features.manage_items.ManageItemsViewModelFactory
import com.stride.cashflow.features.planner.PlannerScreen
import com.stride.cashflow.features.planner.PlannerViewModel
import com.stride.cashflow.ui.theme.StrideCashflowTheme
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    // Keep the factory for ViewModels that don't need arguments
    private val vmFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = (application as StrideApplication).repository
                return when (modelClass) {
                    ManageItemsViewModel::class.java -> ManageItemsViewModel(repo) as T
                    DashboardViewModel::class.java -> DashboardViewModel(repo) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContent {
            StrideCashflowTheme {
                StrideApp(factory = vmFactory)
            }
        }
    }
}

@Composable
fun StrideApp(factory: ViewModelProvider.Factory) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {

        composable("dashboard") {
            // Get the ViewModel using the factory
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToManageItems = {
                    navController.navigate("manage_items")
                },
                onNavigateToPlanner = { month ->
                    // Navigate to the planner screen, passing the month as an argument
                    navController.navigate("planner/$month")
                }
            )
        }

        composable("manage_items") {
            val manageItemsViewModel: ManageItemsViewModel = viewModel(factory = factory)
            ManageItemsScreen(viewModel = manageItemsViewModel)
        }

        // --- ADD THE NEW PLANNER DESTINATION ---
        composable(
            route = "planner/{month}", // The route includes a placeholder for the month
            arguments = listOf(navArgument("month") { type = NavType.StringType })
        ) { backStackEntry ->
            // Get the application context safely
            val context = LocalContext.current.applicationContext
            val repository = (context as StrideApplication).repository

            // The PlannerViewModel is special because it needs SavedStateHandle.
            // The viewModel() composable can create this for us automatically if we
            // provide the arguments in the factory.
            val plannerViewModel: PlannerViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        // Get the SavedStateHandle from the extras
                        val savedStateHandle = extras.createSavedStateHandle()
                        return PlannerViewModel(repository, savedStateHandle) as T
                    }
                }
            )

            val month = backStackEntry.arguments?.getString("month") ?: "Error"
            PlannerScreen(
                month = month,
                viewModel = plannerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}
