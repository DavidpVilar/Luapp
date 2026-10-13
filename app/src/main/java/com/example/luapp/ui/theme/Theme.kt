package com.example.luapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CelesteDark,
    onPrimary = Color.Black,
    primaryContainer = CelestePrimary,
    onPrimaryContainer = Color.Black,
    secondary = LunaYellow,
    onSecondary = Color.Black,
    background = SoftBlueBackground,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = CelestePrimary.copy(alpha = 0.3f)
)

private val DarkColorScheme = LightColorScheme // Keep it same for "infantil" look

@Composable
fun LuappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to keep custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
