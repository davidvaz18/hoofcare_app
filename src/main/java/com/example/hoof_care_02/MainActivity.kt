package com.example.hoof_care_02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.hoof_care_02.navigation.HoofCareNavHost
import com.example.hoof_care_02.ui.theme.HoofCareTheme

/**
 * Activity única do app, hospedando todas as telas migradas para Jetpack Compose
 * via Navigation Compose (HoofCareNavHost). Antes, esta classe só mostrava a
 * splash (activity_main.xml) e abria MainActivity2 via Intent após 3s; essa
 * mesma lógica agora vive em SplashScreen.kt + HoofCareNavHost.kt.
 *
 * As telas ainda não migradas (PaginaHome, UserProfileActivity01, etc.) continuam
 * sendo Activities XML separadas, abertas por Intent a partir do NavHost.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HoofCareTheme {
                HoofCareNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
