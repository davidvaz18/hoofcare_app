package com.example.hoof_care_02.ui.theme

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

private val LightColors = lightColorScheme(
    primary = HoofGreenDark,
    secondary = HoofGreenLight,
    background = HoofWhite,
    surface = HoofWhite,
)

private val DarkColors = darkColorScheme(
    primary = HoofGreenDark,
    secondary = HoofGreenLight,
)

@Composable
fun HoofCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Material You) desligado por padrão para preservar a identidade visual original do app
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
