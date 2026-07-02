package com.example.hoof_care_02.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hoof_care_02.R
import com.example.hoof_care_02.model.Breed
import com.example.hoof_care_02.model.Dog
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.util.UserProfileData
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

private val client = OkHttpClient()
private const val DOGS_URL = "http://10.0.2.2:8000/api/dogs/"

@Composable
fun DogListScreen(
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetProfile: (Int) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var dogList by remember { mutableStateOf<List<Dog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fetchDogs(
            onSuccess = { dogs ->
                dogList = dogs
                isLoading = false
            },
            onError = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
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
            // Header
            HeaderSection(userName = UserProfileData.nomeUsuario ?: "Usuário")

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

private fun fetchDogs(onSuccess: (List<Dog>) -> Unit, onError: (String) -> Unit) {
    val token = UserProfileData.accessToken
    if (token == null) {
        onError("Erro de autenticação.")
        return
    }

    val request = Request.Builder()
        .url(DOGS_URL)
        .addHeader("Authorization", "Bearer $token")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Falha ao buscar pets.")
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                try {
                    val dogs = parseDogs(responseBody)
                    onSuccess(dogs)
                } catch (e: Exception) {
                    onError("Erro ao processar dados.")
                }
            } else {
                onError("Nenhum pet encontrado.")
            }
        }
    })
}

private fun parseDogs(jsonString: String): List<Dog> {
    val dogs = mutableListOf<Dog>()
    val jsonArray = JSONArray(jsonString)
    for (i in 0 until jsonArray.length()) {
        val dogObject = jsonArray.getJSONObject(i)
        val breedObject = dogObject.getJSONObject("breed")
        val breed = Breed(
            id = breedObject.getInt("id"),
            name = breedObject.getString("name")
        )
        val dog = Dog(
            id = dogObject.getInt("id"),
            name = dogObject.getString("name"),
            age = dogObject.getInt("age"),
            sex = dogObject.getString("sex"),
            photo = dogObject.optString("photo", null),
            birthday = dogObject.optString("birthday", null),
            weight = dogObject.optDouble("weight", 0.0),
            breed = breed
        )
        dogs.add(dog)
    }
    return dogs
}
