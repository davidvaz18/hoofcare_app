package com.example.hoof_care_02.ui.theme

import android.app.Activity
import android.os.Build
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
    // O app foi desenhado inteiramente para tema claro (várias telas forçam fundo
    // branco/verde manualmente). Seguir o tema escuro do sistema fazia o texto ficar
    // quase invisível nesses fundos claros, então ignoramos isSystemInDarkTheme()
    // e sempre usamos as cores claras — a não ser que o próprio app ganhe telas
    // desenhadas para dark mode no futuro.
    darkTheme: Boolean = false,
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
