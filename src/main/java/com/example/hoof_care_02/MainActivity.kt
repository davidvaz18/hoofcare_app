package com.example.hoof_care_02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.hoof_care_02.navigation.HoofCareNavHost
import com.example.hoof_care_02.ui.theme.HoofCareTheme

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
