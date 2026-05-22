package com.example.dentflow_android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF509ab4),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004d61),
    onPrimaryContainer = Color(0xFFbce9ff),

    secondary = Color(0xFFb7883e),
    onSecondary = Color(0xFF432c00),
    secondaryContainer = Color(0xFF604100),
    onSecondaryContainer = Color(0xFFffddb3),

    tertiary = Color(0xFF6196c0),
    onTertiary = Color(0xFF003355),

    background = Color(0xFF0f1416),
    onBackground = Color(0xFFe1e2e5),

    surface = Color(0xFF1b2023),
    onSurface = Color(0xFFe1e2e5),
    surfaceVariant = Color(0xFF252b2d),
    onSurfaceVariant = Color(0xFFc0c7cd),

    error = Color(0xFF740006),
    onError = Color(0xFFffd2cc)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF509ab4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFbce9ff),
    onPrimaryContainer = Color(0xFF001f2a),

    secondary = Color(0xFFb7883e),
    onSecondary = Color.White,
    secondaryContainer = Color(0xE9ECCEA8),
    onSecondaryContainer = Color(0xFF291800),

    tertiary = Color(0xFF81A6C6),
    onTertiary = Color.White,

    background = Color(0xFFf5fafd),
    onBackground = Color(0xFF191c1e),

    surface = Color.White,
    onSurface = Color(0xFF191c1e),
    surfaceVariant = Color(0xFFD2C4B4),
    onSurfaceVariant = Color(0xFF40484c),

    error = Color(0xFAD94B42),
    onError = Color(0xFFffd2cc)
)

@Composable
fun DentFlowAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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