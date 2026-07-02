package com.example.hoof_care_02.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.R
import com.example.hoof_care_02.util.UserProfileData

@Composable
fun HomeScreen(
    onNavigateToPets: () -> Unit,
    onNavigateToLembretes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToClinicas: () -> Unit
) {
    val context = LocalContext.current
    val userName = UserProfileData.nomeUsuario ?: "Usuário"
    val selectedPet = UserProfileData.cachorroSelecionado

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                onHomeClick = { /* Já estamos na Home */ },
                onPetsClick = onNavigateToPets,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Top Bar / Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.fotousuario),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateToProfile() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Olá, $userName!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onNavigateToSettings) {
                    Image(
                        painter = painterResource(id = R.drawable.settings_icon),
                        contentDescription = "Configurações",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "De quem falamos hoje?",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pet Selection Button
            Button(
                onClick = onNavigateToPets,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38C075))
            ) {
                Text(
                    text = selectedPet?.name ?: "Selecione um Pet",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Grid of Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Find Vets
                HomeActionButton(
                    text = "Encontre\nClínicas veterinárias próximas de você",
                    modifier = Modifier.weight(1f).height(130.dp),
                    onClick = onNavigateToClinicas
                )

                // Reminders
                HomeActionCard(
                    text = "Lembretes",
                    iconRes = R.drawable.imgalimentacao,
                    modifier = Modifier.weight(1f).height(130.dp),
                    onClick = onNavigateToLembretes
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Health
                HomeActionCard(
                    text = "Saúde",
                    iconRes = R.drawable.imgsaude,
                    modifier = Modifier.weight(1f).height(130.dp),
                    onClick = {
                        if (selectedPet != null) {
                            // Poderia navegar para uma rota específica de saúde
                            Toast.makeText(context, "Saúde disponível na lista de Pets.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Selecione um pet primeiro.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                
                // Placeholder to maintain layout if needed, or just let the Row be uneven.
                // Alternatively, I can expand the "Saúde" card to fill the row if desired, 
                // but keeping it as weight(1f) in a Row with 12dp spacing is safer for now.
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
