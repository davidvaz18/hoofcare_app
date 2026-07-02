package com.example.hoof_care_02.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hoof_care_02.data.repository.PetRepository
import com.example.hoof_care_02.model.Dog
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.util.UserProfileData
import kotlinx.coroutines.launch

@Composable
fun DogListScreen(
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dogList by remember { mutableStateOf<List<Dog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Migração Firebase: Carregando do repositório
                dogList = PetRepository.getDogs()
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar pets.", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            // Regra de negócio do colega: Máximo de 3 pets
            if (dogList.size < 3) {
                FloatingActionButton(
                    onClick = onNavigateToAddPet,
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Pet")
                }
            }
        },
        bottomBar = {
            // Componente centralizado no CommonUI
            AppBottomNavigationBar(
                onHomeClick = onNavigateHome,
                onPetsClick = { /* Já estamos aqui */ },
                onProfileClick = onNavigateToUserProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Componente centralizado no CommonUI
            HeaderSection(
                userName = UserProfileData.nomeUsuario ?: "Usuário",
                onProfileClick = onNavigateToUserProfile
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HoofGreenDark)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(dogList) { dog ->
                        // Componente centralizado no CommonUI
                        DogCard(
                            dog = dog,
                            onVerPerfil = { onNavigateToPetProfile(dog.id) },
                            onSelecionar = {
                                UserProfileData.cachorroSelecionado = dog
                                Toast.makeText(context, "${dog.name} selecionado!", Toast.LENGTH_SHORT).show()
                                onNavigateHome()
                            }
                        )
                    }
                }
            }
        }
    }
}
