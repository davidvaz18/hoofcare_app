package com.example.hoof_care_02.ui.screens

import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.ActivityCard
import com.example.hoof_care_02.ActivityType
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.util.AlarmScheduler
import com.example.hoof_care_02.util.UserProfileData
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

private val client = OkHttpClient()
private const val REMINDERS_URL = "http://10.0.2.2:8000/api/lembretes/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var reminders by remember { mutableStateOf<List<ActivityCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    fun refreshReminders() {
        isLoading = true
        fetchReminders(
            onSuccess = { fetched ->
                reminders = fetched
                isLoading = false
            },
            onError = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        refreshReminders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lembretes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = HoofGreenDark,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Lembrete")
            }
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
            } else if (reminders.isEmpty()) {
                Text(
                    text = "Nenhum lembrete encontrado.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderItem(reminder)
                    }
                }
            }
        }

        if (showDialog) {
            AddReminderDialog(
                onDismiss = { showDialog = false },
                onReminderAdded = {
                    showDialog = false
                    refreshReminders()
                }
            )
        }
    }
}

@Composable
fun ReminderItem(reminder: ActivityCard) {
    val backgroundColor = when (reminder.type) {
        ActivityType.ALIMENTACAO -> Color(0xFF4CAF50)
        ActivityType.ATIVIDADE_FISICA -> Color(0xFFFF9800)
        ActivityType.OUTRO -> Color(0xFF7E57C2)
        else -> Color(0xFF757575)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reminder.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = reminder.description,
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Para: ${reminder.petName}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onReminderAdded: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Comida") }
    var selectedTime by remember { mutableStateOf("Selecionar Horário") }
    var timeForApi by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            timeForApi = String.format(Locale.US, "%02d:%02d:00", hourOfDay, minute)
            selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Lembrete") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedType == "Comida", onClick = { selectedType = "Comida" })
                    Text("Comida", modifier = Modifier.clickable { selectedType = "Comida" })
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = selectedType == "Passeio", onClick = { selectedType = "Passeio" })
                    Text("Passeio", modifier = Modifier.clickable { selectedType = "Passeio" })
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = selectedType == "Outro", onClick = { selectedType = "Outro" })
                    Text("Outro", modifier = Modifier.clickable { selectedType = "Outro" })
                }

                if (selectedType == "Outro") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedTime)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dogId = UserProfileData.cachorroSelecionado?.id
                    if (dogId == null) {
                        Toast.makeText(context, "Selecione um pet na Home primeiro.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (timeForApi.isEmpty()) {
                        Toast.makeText(context, "Selecione um horário.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedType == "Outro" && title.isBlank()) {
                        Toast.makeText(context, "Título é obrigatório para 'Outro'.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val json = JSONObject().apply {
                        put("dog", dogId)
                        put("tipo", selectedType)
                        put("horario", timeForApi)
                        if (selectedType == "Outro") {
                            put("titulo", title)
                            put("descricao", description)
                        }
                    }

                    saveReminder(
                        context = context,
                        json = json,
                        onSuccess = { onReminderAdded() },
                        onError = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
                    )
                }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun fetchReminders(onSuccess: (List<ActivityCard>) -> Unit, onError: (String) -> Unit) {
    val token = UserProfileData.accessToken ?: return onError("Não autenticado")
    val request = Request.Builder()
        .url(REMINDERS_URL)
        .addHeader("Authorization", "Bearer $token")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Falha na conexão.")
        }
        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            if (response.isSuccessful && body != null) {
                val list = mutableListOf<ActivityCard>()
                val array = JSONArray(body)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val type = when (obj.getString("tipo")) {
                        "Comida" -> ActivityType.ALIMENTACAO
                        "Passeio" -> ActivityType.ATIVIDADE_FISICA
                        else -> ActivityType.OUTRO
                    }
                    list.add(ActivityCard(
                        id = obj.getString("id"),
                        title = obj.getString("titulo_display"),
                        description = obj.getString("descricao_display"),
                        type = type,
                        petName = obj.getString("dog_name")
                    ))
                }
                onSuccess(list)
            } else {
                onError("Erro ao carregar lembretes.")
            }
        }
    })
}

private fun saveReminder(
    context: android.content.Context,
    json: JSONObject,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val token = UserProfileData.accessToken ?: return
    val body = json.toString().toRequestBody("application/json".toRequestBody().contentType())
    val request = Request.Builder()
        .url(REMINDERS_URL)
        .post(json.toString().toRequestBody("application/json".toMediaType()))
        .addHeader("Authorization", "Bearer $token")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Erro de conexão.")
        }
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                try {
                    val created = JSONObject(responseBody)
                    val id = created.getInt("id")
                    val horario = created.getString("horario")
                    val h = horario.substring(0, 2).toInt()
                    val m = horario.substring(3, 5).toInt()
                    
                    AlarmScheduler.scheduleRepeatingAlarm(
                        context, h, m, id,
                        created.getString("titulo_display"),
                        created.getString("descricao_display")
                    )
                } catch (e: Exception) {
                    Log.e("Alarm", "Erro agendando alarme", e)
                }
                onSuccess()
            } else {
                onError("Erro ao salvar.")
            }
        }
    })
}
