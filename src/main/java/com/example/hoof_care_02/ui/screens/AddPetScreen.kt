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
import com.example.hoof_care_02.model.ALL_BREEDS
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import kotlinx.coroutines.launch
import java.util.*

/**
 * Calcula a idade em anos completos a partir de uma data de nascimento
 * no formato "yyyy-MM-dd". Retorna 0 se a data for inválida/nula ou no futuro.
 */
fun calcularIdadeAPartirDoNascimento(birthday: String?): Int {
    if (birthday.isNullOrBlank()) return 0
    return try {
        val parts = birthday.split("-")
        val birthYear = parts[0].toInt()
        val birthMonth = parts[1].toInt()
        val birthDay = parts[2].toInt()

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthYear
        val todayMonth = today.get(Calendar.MONTH) + 1
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        if (todayMonth < birthMonth || (todayMonth == birthMonth && todayDay < birthDay)) {
            age--
        }
        if (age < 0) 0 else age
    } catch (e: Exception) {
        0
    }
}

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
    var nameError by remember { mutableStateOf<String?>(null) }
    var breedError by remember { mutableStateOf<String?>(null) }
    var sexError by remember { mutableStateOf<String?>(null) }
    var birthdayError by remember { mutableStateOf<String?>(null) }

    var breedsExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedBreedName) {
        if (selectedBreed?.name != selectedBreedName) {
            val match = ALL_BREEDS.firstOrNull { it.name.equals(selectedBreedName, ignoreCase = true) }
            if (match != null) {
                selectedBreed = match
                breedError = null
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
                    birthdayError = null
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
                onValueChange = { name = it; nameError = null },
                label = { Text("Nome do Pet") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = Color(0xFFD32F2F)) } }
            )


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

                val filteredBreeds = ALL_BREEDS.filter {
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


            if (selectedBreedName.isNotBlank() && selectedBreed == null) {
                Text(
                    text = "Escolha uma raça da lista",
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (breedError != null) {
                Text(
                    text = breedError!!,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            Column(modifier = Modifier.fillMaxWidth()) {
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
                        enabled = !isLoading,
                        isError = sexError != null
                    )
                    DropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(text = { Text("Macho") }, onClick = { sex = "M"; sexError = null; sexExpanded = false })
                        DropdownMenuItem(text = { Text("Fêmea") }, onClick = { sex = "F"; sexError = null; sexExpanded = false })
                    }
                }
                if (sexError != null) {
                    Text(
                        text = sexError!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = { datePickerDialog?.show() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(birthdayDisplay)
            }
            if (birthdayError != null) {
                Text(
                    text = birthdayError!!,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            if (birthdayApi != null) {
                val idadeCalculada = calcularIdadeAPartirDoNascimento(birthdayApi)
                Text(
                    text = "Idade calculada: $idadeCalculada ${if (idadeCalculada == 1) "ano" else "anos"}",
                    color = HoofGreenDark,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
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
                        nameError = if (name.isBlank()) "Nome é obrigatório" else null
                        breedError = if (selectedBreed == null) "Selecione uma raça" else null
                        sexError = if (sex == null) "Selecione o gênero" else null
                        birthdayError = if (birthdayApi == null) "Selecione a data de nascimento" else null

                        if (nameError != null || breedError != null || sexError != null || birthdayError != null) {
                            return@Button
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                val dog = Dog(
                                    id = "",
                                    name = name,
                                    age = calcularIdadeAPartirDoNascimento(birthdayApi),
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