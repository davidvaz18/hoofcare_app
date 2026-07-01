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
    var selectedBreedName by remember { mutableStateOf("Escolha a raça") }

    var breeds by remember { mutableStateOf<List<Breed>>(emptyList()) }
    var breedsExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchBreeds(
            onSuccess = { fetchedBreeds -> breeds = fetchedBreeds },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        )
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            birthdayApi = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            birthdayDisplay = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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

            // Breed Spinner (Exposed Dropdown Menu)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedBreedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Raça") },
                    trailingIcon = {
                        IconButton(onClick = { breedsExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = breedsExpanded,
                    onDismissRequest = { breedsExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    breeds.forEach { breed ->
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
                onClick = { datePickerDialog.show() },
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
                    if (name.isBlank() || selectedBreedId == null || sex == null) {
                        Toast.makeText(context, "Nome, Raça e Gênero são obrigatórios.", Toast.LENGTH_SHORT).show()
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
