package com.example.myfitnessapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FitnessDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF003544),
    primaryContainer = CyanDark,
    onPrimaryContainer = Color.White,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2D2060),
    onSecondaryContainer = PurpleLight,
    tertiary = OrangeAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF5C2800),
    onTertiaryContainer = OrangeLight,
    error = RedAccent,
    onError = Color.White,
    background = DarkBackground,
    onBackground = TextOnDark,
    surface = DarkSurface,
    onSurface = TextOnDark,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextOnDarkSecondary,
    outline = TextOnDarkTertiary,
    inverseSurface = LightSurface,
    inverseOnSurface = TextOnLight
)

private val FitnessLightColorScheme = lightColorScheme(
    primary = CyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF003544),
    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF2D2060),
    tertiary = OrangeAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF5C2800),
    error = RedAccent,
    onError = Color.White,
    background = LightBackground,
    onBackground = TextOnLight,
    surface = LightSurface,
    onSurface = TextOnLight,
    surfaceVariant = LightCardVariant,
    onSurfaceVariant = TextOnLightSecondary,
    outline = Color(0xFF9E9E9E),
    inverseSurface = DarkSurface,
    inverseOnSurface = TextOnDark
)

@Composable
fun MyFitnessAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FitnessDarkColorScheme else FitnessLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}