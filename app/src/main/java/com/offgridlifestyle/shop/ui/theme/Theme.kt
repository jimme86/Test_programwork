package com.offgridlifestyle.shop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = ForestGreen40,
    onPrimary = Sand99,
    primaryContainer = ForestGreen80,
    onPrimaryContainer = ForestGreen20,
    secondary = Amber40,
    onSecondary = Sand99,
    secondaryContainer = Amber80,
    onSecondaryContainer = Bark40,
    tertiary = Bark40,
    onTertiary = Sand99,
    tertiaryContainer = Bark80,
    onTertiaryContainer = Sand10,
    background = Sand99,
    onBackground = Sand10,
    surface = Sand99,
    onSurface = Sand10,
    surfaceVariant = Sand95,
    onSurfaceVariant = Bark40,
    error = Error40,
    onError = Sand99,
    outline = Bark40
)

private val DarkColors = darkColorScheme(
    primary = ForestGreen80,
    onPrimary = ForestGreen20,
    primaryContainer = ForestGreen40,
    onPrimaryContainer = ForestGreen80,
    secondary = Amber80,
    onSecondary = Bark40,
    secondaryContainer = Amber40,
    onSecondaryContainer = Sand99,
    tertiary = Bark80,
    onTertiary = Sand10,
    tertiaryContainer = Bark40,
    onTertiaryContainer = Bark80,
    background = Charcoal10,
    onBackground = Charcoal90,
    surface = Charcoal10,
    onSurface = Charcoal90,
    surfaceVariant = Sand10,
    onSurfaceVariant = Bark80,
    error = Error80,
    onError = Sand10,
    outline = Bark80
)

@Composable
fun OffGridLifestyleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is intentionally off by default so the brand's earthy palette
    // stays consistent across devices instead of following the user's wallpaper.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    // Status/navigation bar colors are left to enableEdgeToEdge() (see MainActivity);
    // here we only need to keep the system bar icons legible against our background.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
