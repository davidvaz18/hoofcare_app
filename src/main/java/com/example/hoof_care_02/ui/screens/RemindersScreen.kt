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
import com.example.hoof_care_02.data.repository.ReminderRepository
import com.example.hoof_care_02.model.Reminder
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.util.AlarmScheduler
import com.example.hoof_care_02.util.UserProfileData
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    val selectedPet = UserProfileData.cachorroSelecionado

    fun refreshReminders() {
        if (selectedPet == null) {
            isLoading = false
            return
        }
        isLoading = true
        scope.launch {
            try {
                reminders = ReminderRepository.getReminders(selectedPet.id)
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar lembretes.", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedPet) {
        refreshReminders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lembretes${selectedPet?.let { " - ${it.name}" } ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedPet != null) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = HoofGreenDark,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Lembrete")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (selectedPet == null) {
                Text(
                    text = "Selecione um pet na Home primeiro.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else if (isLoading) {
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

        if (showDialog && selectedPet != null) {
            AddReminderDialog(
                petId = selectedPet.id,
                petName = selectedPet.name,
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
fun ReminderItem(reminder: Reminder) {
    val backgroundColor = when (reminder.type) {
        "Comida" -> Color(0xFF4CAF50)
        "Passeio" -> Color(0xFFFF9800)
        else -> Color(0xFF7E57C2)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.getDisplayTitle(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reminder.time,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = reminder.getDisplayDescription(),
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
    petId: String,
    petName: String,
    onDismiss: () -> Unit,
    onReminderAdded: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Comida") }
    var selectedTime by remember { mutableStateOf("Selecionar Horário") }
    var timeForSave by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            timeForSave = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
            selectedTime = timeForSave
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    Text(selectedTime)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    if (timeForSave.isEmpty()) {
                        Toast.makeText(context, "Selecione um horário.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedType == "Outro" && title.isBlank()) {
                        Toast.makeText(context, "Título é obrigatório para 'Outro'.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    scope.launch {
                        val reminder = Reminder(
                            petId = petId,
                            petName = petName,
                            type = selectedType,
                            time = timeForSave,
                            title = if (selectedType == "Outro") title else null,
                            description = if (selectedType == "Outro") description else null
                        )

                        val newId = ReminderRepository.saveReminder(reminder)
                        if (newId != null) {
                            // Agenda o alarme local
                            try {
                                val h = timeForSave.substring(0, 2).toInt()
                                val m = timeForSave.substring(3, 5).toInt()
                                
                                AlarmScheduler.scheduleRepeatingAlarm(
                                    context, h, m, newId,
                                    reminder.getDisplayTitle(),
                                    reminder.getDisplayDescription()
                                )
                            } catch (e: Exception) {
                                Log.e("Alarm", "Erro agendando alarme", e)
                            }
                            onReminderAdded()
                        } else {
                            Toast.makeText(context, "Erro ao salvar lembrete.", Toast.LENGTH_SHORT).show()
                            isSaving = false
                        }
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Adicionar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}
