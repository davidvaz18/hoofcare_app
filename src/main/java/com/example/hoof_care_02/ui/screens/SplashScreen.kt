package com.example.hoof_care_02.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.hoof_care_02.R
import com.example.hoof_care_02.ui.theme.HoofGreenLight
import kotlinx.coroutines.delay

/**
 * Equivalente Compose da antiga MainActivity (activity_main.xml).
 * Mostra a logo por 3s e então navega para o Login, exatamente como o
 * Handler(Looper.getMainLooper()).postDelayed(... , 3000) original.
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HoofGreenLight
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.hoofcare),
                contentDescription = null
            )
        }
    }
}
