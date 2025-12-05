package com.stride.cashflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// A sealed class to represent our navigation items.
sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("dashboard", Icons.Outlined.Home, "Planners")
    object Flows : BottomNavItem("manage_items", Icons.Outlined.Settings, "Flows")
}
