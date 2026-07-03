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
import androidx.compose.ui.res.stringResource
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
    onNavigateToClinicas: () -> Unit,
    onNavigateToSaude: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val strUsernameFallback = stringResource(R.string.common_username_fallback)
    val strSelectPetFirst = stringResource(R.string.home_select_pet_first)
    val userName = UserProfileData.nomeUsuario ?: strUsernameFallback
    val selectedPet = UserProfileData.cachorroSelecionado

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                selectedTab = BottomNavTab.HOME,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val photoUrl = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl
                    if (photoUrl != null) {
                        coil.compose.AsyncImage(
                            model = photoUrl,
                            contentDescription = stringResource(R.string.home_user_photo),
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5))
                                .clickable { onNavigateToProfile() },
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.fotousuario),
                            contentDescription = stringResource(R.string.home_user_photo),
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .clickable { onNavigateToProfile() }
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.common_hello, userName),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onNavigateToSettings) {
                    Image(
                        painter = painterResource(id = R.drawable.settings_icon),
                        contentDescription = stringResource(R.string.home_settings),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.home_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToPets,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38C075))
            ) {
                Text(
                    text = selectedPet?.name ?: stringResource(R.string.home_select_pet),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                HomeActionButton(
                    text = stringResource(R.string.home_clinics_card),
                    modifier = Modifier.weight(1f).height(130.dp),
                    onClick = onNavigateToClinicas
                )


                HomeActionCard(
                    text = stringResource(R.string.home_reminders_card),
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

                HomeActionCard(
                    text = stringResource(R.string.home_health_card),
                    iconRes = R.drawable.imgsaude,
                    modifier = Modifier.weight(1f).height(130.dp),
                    onClick = {
                        if (selectedPet != null) {
                            onNavigateToSaude(selectedPet.id)
                        } else {
                            Toast.makeText(context, strSelectPetFirst, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
