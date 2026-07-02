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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.model.Breed
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.util.UserProfileData
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

private val client = OkHttpClient()
private const val DOGS_URL = "http://10.0.2.2:8000/api/dogs/"
private const val BREEDS_URL = "http://10.0.2.2:8000/api/breeds/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var birthdayDisplay by remember { mutableStateOf("Data de Nascimento") }
    var birthdayApi by remember { mutableStateOf<String?>(null) }
    var sex by remember { mutableStateOf<String?>(null) }
    var selectedBreedId by remember { mutableStateOf<Int?>(null) }
    var selectedBreedName by remember { mutableStateOf("") }

    var breeds by remember { mutableStateOf<List<Breed>>(emptyList()) }
    var breedsExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchBreeds(
            onSuccess = { fetchedBreeds -> breeds = fetchedBreeds },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        )
    }

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
                modifier = Modifier.fillMaxWidth()
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
                    placeholder = { Text("Digite para buscar a raça...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                val filteredBreeds = remember(selectedBreedName, breeds) {
                    if (selectedBreedName.isBlank()) {
                        breeds
                    } else {
                        breeds.filter { it.name.contains(selectedBreedName, ignoreCase = true) }
                    }
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
                                    selectedBreedId = breed.id
                                    selectedBreedName = breed.name
                                    breedsExpanded = false
                                }
                            )
                        }
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
                        IconButton(onClick = { sexExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
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
                onClick = {
                    try {
                        datePickerDialog?.show()
                            ?: Toast.makeText(context, "Não foi possível abrir o seletor de data.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Não foi possível abrir o seletor de data.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(birthdayDisplay)
            }

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val camposFaltando = buildList {
                        if (name.isBlank()) add("Nome")
                        if (selectedBreedId == null) {
                            add(
                                if (selectedBreedName.isBlank()) {
                                    "Raça"
                                } else {
                                    "Raça (toque em uma sugestão da lista ou digite o nome exato de uma raça cadastrada)"
                                }
                            )
                        }
                        if (sex == null) add("Gênero")
                    }

                    if (camposFaltando.isNotEmpty()) {
                        Toast.makeText(
                            context,
                            "Campo(s) obrigatório(s) faltando: ${camposFaltando.joinToString(", ")}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        registrarCachorro(
                            name = name,
                            breedId = selectedBreedId!!,
                            sex = sex!!,
                            weight = weight.toDoubleOrNull(),
                            birthday = birthdayApi,
                            onSuccess = {
                                Toast.makeText(context, "Pet cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            },
                            onError = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
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

private fun fetchBreeds(onSuccess: (List<Breed>) -> Unit, onError: (String) -> Unit) {
    val request = Request.Builder().url(BREEDS_URL).build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Falha ao carregar raças.")
        }
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                val jsonArray = JSONArray(responseBody)
                val fetchedBreeds = mutableListOf<Breed>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    fetchedBreeds.add(Breed(obj.getInt("id"), obj.getString("name")))
                }
                onSuccess(fetchedBreeds)
            }
        }
    })
}

private fun registrarCachorro(
    name: String,
    breedId: Int,
    sex: String,
    weight: Double?,
    birthday: String?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val token = UserProfileData.accessToken ?: return

    val json = JSONObject().apply {
        put("name", name)
        put("breed_id", breedId)
        put("sex", sex)
        put("age", 0)
        if (weight != null) put("weight", weight)
        if (birthday != null) put("birthday", birthday)
    }

    val requestBody = json.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(DOGS_URL)
        .post(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Erro de conexão: ${e.message}")
        }
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("Erro ao cadastrar pet: ${response.body?.string()}")
            }
        }
    })
}
