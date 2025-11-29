package com.stride.cashflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.stride.cashflow.features.onboarding.OnboardingFlow
import com.stride.cashflow.features.onboarding.OnboardingViewModel
import com.stride.cashflow.features.planner.PlannerScreen
import com.stride.cashflow.features.planner.PlannerViewModel
import com.stride.cashflow.ui.theme.StrideCashflowTheme
import com.stride.cashflow.utils.OnboardingManager
import java.time.YearMonth

// The corrected, robust ViewModel Factory
object AppViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        val repository = (application as StrideApplication).repository

        // We check the class type and return the correctly cast ViewModel.
        // This structure is clearer for the compiler.
        if (modelClass.isAssignableFrom(ManageItemsViewModel::class.java)) {
            return ManageItemsViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            return OnboardingViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            return PlannerViewModel(repository, savedStateHandle) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Splash screen is handled by the theme in the manifest, no code needed here
        // if you are using installSplashScreen() you need to import it.
        // For now, let's assume it's handled by the theme to avoid import errors.

        setContent {
            StrideCashflowTheme {
                val context = LocalContext.current
                var showOnboarding by remember { mutableStateOf(!OnboardingManager.hasSeenOnboarding(context)) }

                if (showOnboarding) {
                    OnboardingFlow {
                        // This lambda is called when onboarding is complete
                        OnboardingManager.setOnboardingSeen(context, true)
                        showOnboarding = false
                    }
                } else {
                    StrideApp(factory = AppViewModelFactory)
                }
            }
        }
    }
}

@Composable
fun StrideApp(factory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    // This state ensures we only try to navigate on the very first composition after onboarding
    val isFirstLaunchAfterOnboarding = remember { mutableStateOf(true) }

    NavHost(navController = navController, startDestination = "dashboard") {

        composable("dashboard") {
            // This side-effect runs only when this composable enters the screen
            if (isFirstLaunchAfterOnboarding.value) {
                // We use LaunchedEffect to ensure navigation happens safely after composition
                LaunchedEffect(Unit) {
                    val currentMonth = YearMonth.now()
                    val monthString = "%d-%02d".format(currentMonth.year, currentMonth.monthValue)
                    // Navigate to the create/edit screen for the current month
                    navController.navigate("planner/create/$monthString")
                    // Flip the flag so this doesn't run again
                    isFirstLaunchAfterOnboarding.value = false
                }
            }

            // This ViewModel is composed whether we navigate away or not
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToManageItems = { navController.navigate("manage_items") },
                onNavigateToPlanner = { route -> navController.navigate(route) }
            )
        }

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
