package com.example.hoof_care_02.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hoof_care_02.R
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicasScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isLocating by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            searchNearbyClinics(fusedLocationClient, context) { isLocating = it }
        } else {
            Toast.makeText(context, "Permissão de localização negada.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clínicas Veterinárias") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = HoofGreenDark
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Encontre ajuda profissional",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Clique no botão abaixo para encontrar as clínicas veterinárias mais próximas de você no Google Maps.",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (isLocating) {
                CircularProgressIndicator(color = HoofGreenDark)
            } else {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            searchNearbyClinics(fusedLocationClient, context) { isLocating = it }
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HoofGreenDark)
                ) {
                    Text("Buscar Clínicas Próximas", fontSize = 18.sp, color = Color.White)
                }
            }
            
            TextButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Gerenciar Permissões", color = Color.Gray)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun searchNearbyClinics(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    context: android.content.Context,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            setLoading(false)
            val lat = location?.latitude
            val lng = location?.longitude
            if (lat != null && lng != null) {
                openMaps(context, lat, lng)
            } else {
                // Fallback to last location
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        openMaps(context, lastLoc.latitude, lastLoc.longitude)
                    } else {
                        Toast.makeText(context, "Não foi possível obter sua localização. Verifique o GPS.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        .addOnFailureListener {
            setLoading(false)
            Toast.makeText(context, "Erro ao obter localização: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}

private fun openMaps(context: android.content.Context, lat: Double, lng: Double) {
    val uri = Uri.parse("geo:$lat,$lng?q=clínicas veterinárias")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/clínicas+veterinárias/@$lat,$lng,15z"))
        context.startActivity(browserIntent)
    }
}
