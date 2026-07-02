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
import com.example.hoof_care_02.data.repository.PetRepository
import com.example.hoof_care_02.model.Dog
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.util.UserProfileData
import kotlinx.coroutines.launch

@Composable
fun DogListScreen(
    onNavigateToAddPet: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
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
                onAddPetClick = onNavigateToAddPet,
                onProfileClick = { /* Já estamos na lista de pets */ }
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

@Composable
fun HeaderSection(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.fotousuario),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Olá, $userName!", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Meus Pets", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DogCard(
    dog: Dog,
    onVerPerfil: () -> Unit,
    onSelecionar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF38C075))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = dog.photo ?: R.drawable.ic_launcher_background,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = dog.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = dog.breed.name, color = Color.White, fontSize = 16.sp)
                    Text(text = "${dog.age} anos", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.4f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onVerPerfil) {
                    Text("Ver Perfil", color = Color.White)
                }
                TextButton(onClick = onSelecionar) {
                    Text("Selecionar", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    onHomeClick: () -> Unit,
    onAddPetClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Adicionado o background color para que a área da barra de sistema não fique vazia
    Surface(
        color = Color(0xFF38C075), // Cor base da barra
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding() // Resolve o problema da sobreposição
                .fillMaxWidth()
                .height(75.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.barranavegacao),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Image(
                    painter = painterResource(id = R.drawable.botaohome),
                    contentDescription = "Home",
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onHomeClick() }
                )
                Image(
                    painter = painterResource(id = R.drawable.botaomaiscachorro),
                    contentDescription = "Adicionar",
                    modifier = Modifier
                        .height(85.dp)
                        .weight(1f)
                        .clickable { onAddPetClick() }
                )
                Image(
                    painter = painterResource(id = R.drawable.botaoprofile),
                    contentDescription = "Perfil",
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onProfileClick() }
                )
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
