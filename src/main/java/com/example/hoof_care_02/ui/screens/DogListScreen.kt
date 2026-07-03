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
    var dogPendingDelete by remember { mutableStateOf<Dog?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    fun loadDogs() {
        scope.launch {
            try {
                dogList = PetRepository.getDogs()
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar pets.", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        loadDogs()
    }

    Scaffold(
        floatingActionButton = {
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
            HeaderSection(
                userName = UserProfileData.nomeUsuario ?: "Usuário",
                onProfileClick = onNavigateToUserProfile
            )

            if (!isLoading && dogList.size >= 3) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Text(
                        text = "Você atingiu o limite de 3 pets cadastrados. Exclua um pet (ícone de lixeira no card) para poder adicionar outro.",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF8A5A00)
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HoofGreenDark)
                }
            } else if (dogList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum pet cadastrado ainda.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(dogList, key = { it.id }) { dog ->
                        DogCard(
                            dog = dog,
                            onVerPerfil = { onNavigateToPetProfile(dog.id) },
                            onSelecionar = {
                                UserProfileData.cachorroSelecionado = dog
                                Toast.makeText(context, "${dog.name} selecionado!", Toast.LENGTH_SHORT).show()
                                onNavigateHome()
                            },
                            onExcluir = { dogPendingDelete = dog }
                        )
                    }
                }
            }
        }

        val dogToDelete = dogPendingDelete
        if (dogToDelete != null) {
            AlertDialog(
                onDismissRequest = { if (!isDeleting) dogPendingDelete = null },
                title = { Text("Excluir pet") },
                text = { Text("Tem certeza que deseja excluir ${dogToDelete.name}? Essa ação não pode ser desfeita.") },
                confirmButton = {
                    TextButton(
                        enabled = !isDeleting,
                        onClick = {
                            isDeleting = true
                            scope.launch {
                                val result = PetRepository.deleteDog(dogToDelete.id)
                                if (result.isSuccess) {
                                    if (UserProfileData.cachorroSelecionado?.id == dogToDelete.id) {
                                        UserProfileData.cachorroSelecionado = null
                                    }
                                    Toast.makeText(context, "${dogToDelete.name} excluído.", Toast.LENGTH_SHORT).show()
                                    dogPendingDelete = null
                                    isDeleting = false
                                    loadDogs()
                                } else {
                                    Toast.makeText(context, "Erro ao excluir: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    isDeleting = false
                                }
                            }
                        }
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Excluir", color = Color.Red)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dogPendingDelete = null }, enabled = !isDeleting) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}