package com.example.hoof_care_02.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.data.repository.PetRepository
import com.example.hoof_care_02.model.Dog
import kotlinx.coroutines.launch

private val HealthGreenDark = Color(0xFF2FAE55)
private val HealthBg = Color(0xFFF5F6F7)

@Composable
fun EditPetHealthScreenRoute(
    petId: String,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var dog by remember { mutableStateOf<Dog?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(petId) {
        scope.launch {
            try {
                dog = PetRepository.getDogById(petId)
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HealthGreenDark)
        }
    } else if (dog != null) {
        EditPetHealthScreen(
            dog = dog!!,
            onBack = onBack,
            onSaveSuccess = onSaveSuccess
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pet não encontrado.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetHealthScreen(
    dog: Dog,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var peso by remember { mutableStateOf(dog.weight?.toString() ?: "") }
    var porte by remember { mutableStateOf(dog.porte ?: "") }
    var tipoSanguineo by remember { mutableStateOf(dog.tipoSanguineo ?: "") }
    var pelagem by remember { mutableStateOf(dog.pelagem ?: "") }
    var castrado by remember { mutableStateOf(dog.castrado ?: "Não informado") }
    var descricaoLivre by remember { mutableStateOf(dog.descricaoLivre ?: "") }
    var veterinarioNome by remember { mutableStateOf(dog.veterinarioNome ?: "") }
    var veterinarioClinica by remember { mutableStateOf(dog.veterinarioClinica ?: "") }
    var veterinarioTelefone by remember { mutableStateOf(dog.veterinarioTelefone ?: "") }

    var castradoExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = HealthBg,
        topBar = {
            TopAppBar(
                title = { Text("Editar Ficha Médica", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Informações de Saúde", fontWeight = FontWeight.Bold, color = HealthGreenDark)

            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = porte,
                onValueChange = { porte = it },
                label = { Text("Porte (Ex: Pequeno, Médio, Grande)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tipoSanguineo,
                onValueChange = { tipoSanguineo = it },
                label = { Text("Tipo Sanguíneo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pelagem,
                onValueChange = { pelagem = it },
                label = { Text("Pelagem (Ex: Curta, Longa, Marrom)") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = castrado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Castrado") },
                    trailingIcon = {
                        IconButton(onClick = { castradoExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Selecionar opção")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = castradoExpanded,
                    onDismissRequest = { castradoExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    listOf("Sim", "Não", "Não informado").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                castrado = option
                                castradoExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = descricaoLivre,
                onValueChange = { descricaoLivre = it },
                label = { Text("Descrição Livre / Observações") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Veterinário", fontWeight = FontWeight.Bold, color = HealthGreenDark)

            OutlinedTextField(
                value = veterinarioNome,
                onValueChange = { veterinarioNome = it },
                label = { Text("Nome do Veterinário") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = veterinarioClinica,
                onValueChange = { veterinarioClinica = it },
                label = { Text("Clínica / Hospital") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = veterinarioTelefone,
                onValueChange = { veterinarioTelefone = it },
                label = { Text("Telefone do Veterinário") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        val updatedDog = dog.copy(
                            weight = peso.toDoubleOrNull(),
                            porte = porte,
                            tipoSanguineo = tipoSanguineo,
                            pelagem = pelagem,
                            castrado = castrado,
                            descricaoLivre = descricaoLivre,
                            veterinarioNome = veterinarioNome,
                            veterinarioClinica = veterinarioClinica,
                            veterinarioTelefone = veterinarioTelefone
                        )
                        val result = PetRepository.saveDog(updatedDog)
                        if (result.isSuccess) {
                            Toast.makeText(context, "Ficha atualizada!", Toast.LENGTH_SHORT).show()
                            onSaveSuccess()
                        } else {
                            Toast.makeText(context, "Erro ao salvar: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HealthGreenDark),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Salvar Alterações", color = Color.White)
                }
            }
        }
    }
}
