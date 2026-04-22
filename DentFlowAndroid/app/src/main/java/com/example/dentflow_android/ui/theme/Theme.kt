package com.example.dentflow_android.ui.theme

import android.app.Activity
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
    secondary = Color(0xFFb7883e),
    tertiary = Color(0xFF6196c0),
    background = Color(0xFF0f1416),
    error = Color(0xFF740006),
    onError = Color(0xFFffd2cc),
    surface = Color(0xFF1b2023),
    surfaceVariant = Color(0xFF252b2d)

    )

private val LightColorScheme = lightColorScheme(
    //primary = Color(0xFFAACDDC),
    primary = Color(0xFF509ab4),
    surfaceVariant = Color(0xFFD2C4B4),  // Koralowy akcent
    secondary = Color(0xFFb7883e),
    tertiary = Color(0xFF81A6C6),   // Ciemny granat do detali
    //background = Color(0xFFF3E3D0),
    background = Color(0xFFf5fafd),
    error = Color(0xFFff5449),
    onError = Color(0xFFffd2cc),
    surface = Color.White           // Białe kafelki wizyt

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun DentFlowAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
