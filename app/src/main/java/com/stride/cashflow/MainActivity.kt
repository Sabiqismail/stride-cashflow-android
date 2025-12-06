package com.stride.cashflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stride.cashflow.features.dashboard.DashboardScreen
import com.stride.cashflow.features.dashboard.DashboardViewModel
import com.stride.cashflow.features.dashboard.MonthSliderDialog
import com.stride.cashflow.features.manage_items.ManageItemsScreen
import com.stride.cashflow.features.manage_items.ManageItemsViewModel
import com.stride.cashflow.features.planner.PlannerScreen
import com.stride.cashflow.features.planner.PlannerViewModel
import com.stride.cashflow.ui.theme.StrideCashflowTheme
import java.time.YearMonth

object AppViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        val repository = (application as StrideApplication).repository

        return when {
            modelClass.isAssignableFrom(ManageItemsViewModel::class.java) -> ManageItemsViewModel(repository) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repository) as T
            // THE LINE FOR OnboardingViewModel HAS BEEN REMOVED
            modelClass.isAssignableFrom(PlannerViewModel::class.java) -> {
                val savedStateHandle = extras.createSavedStateHandle()
                PlannerViewModel(repository, savedStateHandle) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StrideCashflowTheme {
                StrideApp(factory = AppViewModelFactory)
            }
        }
    }
}

@Composable
fun StrideApp(factory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    var showAddPlannerDialog by remember { mutableStateOf(false) }

    // --- FIX STEP 1: Get the current navigation state ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // --- FIX STEP 2: Get the current screen's route name (e.g., "dashboard") ---
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomAppBar {
                val currentDestination = navBackStackEntry?.destination
                val items = listOf(BottomNavItem.Home, BottomNavItem.Flows)

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            // --- FIX STEP 3: Add this 'if' condition ---
            // This will only show the FAB if the current route is "dashboard".
            if (currentRoute == "dashboard") {
                FloatingActionButton(
                    onClick = { showAddPlannerDialog = true },
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, "Add New Planner")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavHost(navController = navController, factory = factory)
        }

        if (showAddPlannerDialog) {
            MonthSliderDialog(
                onDismiss = { showAddPlannerDialog = false },
                onConfirm = { month, year ->
                    val monthString = "%d-%02d".format(year, month)
                    navController.navigate("planner/create/$monthString")
                    showAddPlannerDialog = false
                }
            )
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, factory: ViewModelProvider.Factory) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToPlanner = { route -> navController.navigate(route) }
            )
        }

        // Your ManageItemsScreen uses the route "manage_items"
        composable("manage_items") {
            val manageItemsViewModel: ManageItemsViewModel = viewModel(factory = factory)
            ManageItemsScreen(viewModel = manageItemsViewModel)
        }

        composable(
            route = "planner/{month}",
            arguments = listOf(navArgument("month") { type = NavType.StringType })
        ) {
            val plannerViewModel: PlannerViewModel = viewModel(factory = factory)
            val month = it.arguments?.getString("month") ?: "Error"
            PlannerScreen(
                isCreateMode = false,
                month = month,
                viewModel = plannerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveComplete = { /* Not used */ },
                onNavigateToEdit = { route -> navController.navigate(route) }
            )
        }

        composable(
            route = "planner/create/{month}?isCreate={isCreate}",
            arguments = listOf(
                navArgument("month") { type = NavType.StringType },
                navArgument("isCreate") { defaultValue = "true" }
            )
        ) {
            val plannerViewModel: PlannerViewModel = viewModel(factory = factory)
            val month = it.arguments?.getString("month") ?: "Error"
            PlannerScreen(
                isCreateMode = true,
                month = month,
                viewModel = plannerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() },
                onNavigateToEdit = { /* Not used */ }
            )
        }
    }
}
