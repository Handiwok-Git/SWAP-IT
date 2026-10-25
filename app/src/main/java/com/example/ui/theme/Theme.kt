package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SwapItGreenPrimaryDark,
    onPrimary = SwapItGreenOnPrimaryDark,
    primaryContainer = SwapItGreenContainerDark,
    onPrimaryContainer = SwapItGreenOnContainerDark,
    secondary = SwapItSecondaryDark,
    background = SwapItBackgroundDark,
    surface = SwapItSurfaceDark,
    surfaceVariant = SwapItSurfaceVariantDark,
    outline = SwapItOutlineDark,
    onBackground = SwapItTextPrimaryDark,
    onSurface = SwapItTextPrimaryDark,
    onSurfaceVariant = SwapItTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = SwapItGreenPrimary,
    onPrimary = SwapItGreenOnPrimary,
    primaryContainer = SwapItGreenContainer,
    onPrimaryContainer = SwapItGreenOnContainer,
    secondary = SwapItSecondary,
    onSecondary = SwapItOnSecondary,
    secondaryContainer = SwapItSecondaryContainer,
    onSecondaryContainer = SwapItOnSecondaryContainer,
    tertiary = SwapItAmberTertiary,
    onTertiary = SwapItOnTertiary,
    tertiaryContainer = SwapItAmberContainer,
    onTertiaryContainer = SwapItOnTertiaryContainer,
    background = SwapItBackgroundLight,
    surface = SwapItSurfaceLight,
    surfaceVariant = SwapItSurfaceVariantLight,
    outline = SwapItOutlineLight,
    onBackground = SwapItTextPrimaryLight,
    onSurface = SwapItTextPrimaryLight,
    onSurfaceVariant = SwapItTextSecondaryLight
)

@Composable
fun SwapItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand identity by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SwapItTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
