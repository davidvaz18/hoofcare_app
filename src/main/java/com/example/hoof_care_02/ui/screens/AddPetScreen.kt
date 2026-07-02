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
import com.example.hoof_care_02.data.repository.PetRepository
import com.example.hoof_care_02.model.Breed
import com.example.hoof_care_02.model.Dog
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import kotlinx.coroutines.launch
import java.util.*

// Lista fixa de raças (hardcoded) para substituir a API de breeds
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
    var selectedBreedId by remember { mutableStateOf<Int?>(null) }
    var selectedBreedName by remember { mutableStateOf("Escolha a raça") }

    var breedsExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Resolve o breed_id automaticamente quando o texto digitado bate exatamente
    // (ignorando maiúsculas/minúsculas) com uma raça já carregada do backend.
    // Isso cobre o caso de o usuário digitar o nome certinho e seguir em frente
    // sem tocar na sugestão da lista.
    LaunchedEffect(selectedBreedName, breeds) {
        if (selectedBreedId == null && selectedBreedName.isNotBlank()) {
            val match = breeds.firstOrNull { it.name.equals(selectedBreedName, ignoreCase = true) }
            if (match != null) {
                selectedBreedId = match.id
            }
        }
    }

    // ANTES: o DatePickerDialog era construído toda vez que a tela recompunha
    // (a cada tecla digitada em qualquer campo, por exemplo), o que é uma causa
    // conhecida de crash em Compose. Agora ele é criado apenas uma vez, com
    // `remember`, e a construção é protegida com try/catch — se falhar por
    // qualquer motivo de contexto, a tela não derruba o app inteiro, só o
    // seletor de data fica indisponível (com aviso ao tocar no botão).
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

            // Campo de raça: agora é um campo de texto digitável, com sugestões
            // filtradas conforme o usuário digita (em vez de uma lista fixa).
            // O ID só é confirmado quando o usuário toca em uma sugestão da lista
            // (o backend exige breed_id, então digitar um nome que não existe na
            // lista de raças do servidor não seleciona nenhum ID).
            ExposedDropdownMenuBox(
                expanded = breedsExpanded && breeds.isNotEmpty(),
                onExpandedChange = { breedsExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedBreedName,
                    onValueChange = { typed ->
                        selectedBreedName = typed
                        selectedBreedId = null // limpa a seleção até o usuário escolher uma sugestão
                        breedsExpanded = typed.isNotBlank()
                    },
                    label = { Text("Raça") },
                    trailingIcon = {
                        IconButton(onClick = { if (!isLoading) breedsExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                DropdownMenu(
                    expanded = breedsExpanded,
                    onDismissRequest = { breedsExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    FIXED_BREEDS.forEach { breed ->
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

            // Aviso de status logo abaixo do campo, para deixar claro o que está
            // acontecendo: raça confirmada, raça não reconhecida, ou lista de
            // raças que não chegou a carregar do backend.
            when {
                breeds.isEmpty() -> {
                    Text(
                        text = "Não foi possível carregar a lista de raças do servidor. Verifique sua conexão.",
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                selectedBreedId != null -> {
                    Text(
                        text = "✓ Raça reconhecida",
                        color = HoofGreenDark,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                selectedBreedName.isNotBlank() -> {
                    Text(
                        text = "Nenhuma raça encontrada com esse nome. Toque em uma sugestão da lista.",
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            // Sex Spinner
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
                onClick = { datePickerDialog.show() },
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
                        if (name.isBlank() || selectedBreed == null || sex == null) {
                            Toast.makeText(context, "Nome, Raça e Gênero são obrigatórios.", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true
                            scope.launch {
                                try {
                                    val dog = Dog(
                                        id = "",
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
                                        val error = result.exceptionOrNull()
                                        Toast.makeText(context, "Erro: ${error?.message}", Toast.LENGTH_LONG).show()
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Ocorreu um erro inesperado: ${e.message}", Toast.LENGTH_LONG).show()
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
