// In ui/theme/Theme.kt

package com.stride.cashflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define the Light Color Scheme using your brand colors
private val LightColorScheme = lightColorScheme(
    primary = SageGreen,        // Main interactive color (buttons, FABs)
    secondary = Copper,         // Accent color
    background = OffWhite,      // App background
    surface = OffWhite,         // Card backgrounds
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SoftCharcoal, // Text color on background
    onSurface = SoftCharcoal     // Text color on cards
)

@Composable
fun StrideCashflowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Stride is a light-theme-only app as per your design philosophy for calmness.
    // We will ignore the darkTheme parameter for now.
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // This comes from Type.kt
        content = content
    )
}
