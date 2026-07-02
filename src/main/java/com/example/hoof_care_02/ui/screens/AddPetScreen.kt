package com.example.hoof_care_02.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.data.repository.PetRepository
import com.example.hoof_care_02.model.Breed
import com.example.hoof_care_02.model.Dog
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import kotlinx.coroutines.launch
import java.util.*

// Lista fixa de raças (pode ser migrada para o Firestore no futuro)
private val FIXED_BREEDS = listOf(
    Breed("1", "Labrador"),
    Breed("2", "Poodle"),
    Breed("3", "Bulldog"),
    Breed("4", "Golden Retriever"),
    Breed("5", "Pastor Alemão"),
    Breed("6", "Yorkshire"),
    Breed("7", "Beagle"),
    Breed("8", "Shih Tzu"),
    Breed("9", "Chihuahua"),
    Breed("10", "Vira-lata")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var birthdayDisplay by remember { mutableStateOf("Data de Nascimento") }
    var birthdayApi by remember { mutableStateOf<String?>(null) }
    var sex by remember { mutableStateOf<String?>(null) }
    var selectedBreed by remember { mutableStateOf<Breed?>(null) }
    var selectedBreedName by remember { mutableStateOf("") }

    var breedsExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Tenta encontrar a raça automaticamente se o usuário digitar o nome exato
    LaunchedEffect(selectedBreedName) {
        if (selectedBreed?.name != selectedBreedName) {
            val match = FIXED_BREEDS.firstOrNull { it.name.equals(selectedBreedName, ignoreCase = true) }
            if (match != null) {
                selectedBreed = match
            }
        }
    }

    val datePickerDialog = remember {
        try {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    birthdayApi = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    birthdayDisplay = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Pet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Pet") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            // Campo de raça pesquisável (UI do colega integrada ao PetRepository)
            ExposedDropdownMenuBox(
                expanded = breedsExpanded,
                onExpandedChange = { if (!isLoading) breedsExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedBreedName,
                    onValueChange = { 
                        selectedBreedName = it
                        selectedBreed = null 
                    },
                    label = { Text("Raça") },
                    placeholder = { Text("Digite para buscar...") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedsExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                val filteredBreeds = FIXED_BREEDS.filter { 
                    it.name.contains(selectedBreedName, ignoreCase = true) 
                }

                if (filteredBreeds.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = breedsExpanded,
                        onDismissRequest = { breedsExpanded = false }
                    ) {
                        filteredBreeds.forEach { breed ->
                            DropdownMenuItem(
                                text = { Text(breed.name) },
                                onClick = {
                                    selectedBreed = breed
                                    selectedBreedName = breed.name
                                    breedsExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Feedback de validação da raça
            if (selectedBreedName.isNotBlank()) {
                Text(
                    text = if (selectedBreed != null) "✓ Raça reconhecida" else "Escolha uma raça da lista",
                    color = if (selectedBreed != null) HoofGreenDark else Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Gênero
            Box(modifier = Modifier.fillMaxWidth()) {
                val sexDisplay = when(sex) {
                    "M" -> "Macho"
                    "F" -> "Fêmea"
                    else -> "Escolha o gênero"
                }
                OutlinedTextField(
                    value = sexDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gênero") },
                    trailingIcon = {
                        IconButton(onClick = { if (!isLoading) sexExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                DropdownMenu(
                    expanded = sexExpanded,
                    onDismissRequest = { sexExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(text = { Text("Macho") }, onClick = { sex = "M"; sexExpanded = false })
                    DropdownMenuItem(text = { Text("Fêmea") }, onClick = { sex = "F"; sexExpanded = false })
                }
            }

            OutlinedButton(
                onClick = { datePickerDialog?.show() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(birthdayDisplay)
            }

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            if (isLoading) {
                CircularProgressIndicator(color = HoofGreenDark)
            } else {
                Button(
                    onClick = {
                        val missingFields = mutableListOf<String>()
                        if (name.isBlank()) missingFields.add("Nome")
                        if (selectedBreed == null) missingFields.add("Raça")
                        if (sex == null) missingFields.add("Gênero")

                        if (missingFields.isNotEmpty()) {
                            Toast.makeText(context, "Faltando: ${missingFields.joinToString(", ")}", Toast.LENGTH_LONG).show()
                        } else {
                            isLoading = true
                            scope.launch {
                                try {
                                    val dog = Dog(
                                        id = "", // Firestore gera automático
                                        name = name,
                                        age = 0,
                                        sex = sex!!,
                                        photo = null,
                                        birthday = birthdayApi,
                                        weight = weight.toDoubleOrNull(),
                                        breed = selectedBreed!!
                                    )
                                    val result = PetRepository.saveDog(dog)
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "Pet cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                                        onSuccess()
                                    } else {
                                        Toast.makeText(context, "Erro: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HoofGreenDark)
                ) {
                    Text("Adicionar Pet", color = Color.White)
                }
            }
        }
    }
}
