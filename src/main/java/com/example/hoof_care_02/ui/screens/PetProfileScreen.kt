package com.example.hoof_care_02.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hoof_care_02.R
import com.example.hoof_care_02.data.repository.PetRepository
import com.example.hoof_care_02.model.Dog
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    petId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dog by remember { mutableStateOf<Dog?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(petId) {
        scope.launch {
            try {
                dog = PetRepository.getDogById(petId)
                if (dog == null) {
                    Toast.makeText(context, "Pet não encontrado.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar detalhes.", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil do Pet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = HoofGreenDark)
            } else if (dog != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pet Photo
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (dog?.photo != null) {
                            AsyncImage(
                                model = dog?.photo,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_background),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        IconButton(
                            onClick = { Toast.makeText(context, "Editar foto em breve", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.background(HoofGreenDark, CircleShape).size(40.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dog!!.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { Toast.makeText(context, "Editar nome em breve", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Breed Chip
                    SuggestionChip(
                        onClick = { },
                        label = { Text(dog!!.breed.name) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFE8F5E9))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ProfileDetailRow("Gênero", if (dog!!.sex == "M") "Macho" else "Fêmea") {
                                Toast.makeText(context, "Editar gênero em breve", Toast.LENGTH_SHORT).show()
                            }
                            ProfileDetailRow("Idade", if (dog!!.age == 1) "1 ano" else "${dog!!.age} anos") {
                                Toast.makeText(context, "Editar idade em breve", Toast.LENGTH_SHORT).show()
                            }
                            ProfileDetailRow("Peso", "${dog!!.weight ?: 0.0} kg") {
                                Toast.makeText(context, "Editar peso em breve", Toast.LENGTH_SHORT).show()
                            }
                            ProfileDetailRow("Aniversário", formatBirthday(dog!!.birthday)) {
                                Toast.makeText(context, "Editar aniversário em breve", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
        }
    }
}

private fun formatBirthday(birthday: String?): String {
    if (birthday == null) return "Não informado"
    return try {
        val backendFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
        val date = backendFormat.parse(birthday)
        if (date != null) displayFormat.format(date) else birthday
    } catch (e: Exception) {
        birthday
    }
}
