package com.example.hoof_care_02.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.R
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.util.UserProfileData
import java.util.*

@Composable
fun ProfileStepLayout(
    step: Int,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Back Button
        IconButton(onClick = onBack, modifier = Modifier.size(60.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }

        // Progress Bar
        LinearProgressIndicator(
            progress = { (step / 4f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(horizontal = 16.dp),
            color = Color(0xFF38C075),
            trackColor = Color(0xFFE0E0E0),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun UserProfileScreen01(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    ProfileStepLayout(step = 1, onBack = onBack) {
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            painter = painterResource(id = R.drawable.cachorro_acariciado_png),
            contentDescription = null,
            modifier = Modifier.size(320.dp, 135.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Qual seria a melhor \n descrição para você?",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(32.dp))

        val options = listOf("Acabei de adotar um cão.", "O meu cão vive comigo.", "Futuro tutor de pet.")
        options.forEach { option ->
            Button(
                onClick = onNext,
                modifier = Modifier
                    .width(300.dp)
                    .height(55.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color.Black
                )
            ) {
                Text(text = option, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun UserProfileScreen02(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    ProfileStepLayout(step = 2, onBack = onBack) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(
            text = "Seu pet é um...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            GenderButton(text = "Menino", onClick = {
                UserProfileData.generoCao = "Menino"
                onNext()
            })
            GenderButton(text = "Menina", onClick = {
                UserProfileData.generoCao = "Menina"
                onNext()
            })
        }
    }
}

@Composable
fun GenderButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(140.dp, 60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HoofGreenDark)
    ) {
        Text(text = text, fontSize = 18.sp, color = Color.White)
    }
}

@Composable
fun UserProfileScreen03(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var dateDisplay by remember { mutableStateOf("Data de Nascimento") }
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
            dateDisplay = formatted
            UserProfileData.dataNascimentoCao = formatted
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    ProfileStepLayout(step = 3, onBack = onBack) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "Quando ele nasceu?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = dateDisplay, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (UserProfileData.dataNascimentoCao != null) {
                    onNext()
                } else {
                    Toast.makeText(context, "Selecione uma data", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HoofGreenDark)
        ) {
            Text("Seguir", color = Color.White)
        }

        TextButton(onClick = {
            UserProfileData.dataNascimentoCao = null
            onNext()
        }) {
            Text("Não sei a data exata", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen04(
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var breed by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val racas = listOf(
        "Labrador", "Poodle", "Bulldog", "Golden Retriever", "Pastor Alemão",
        "Yorkshire", "Beagle", "Shih Tzu", "Chihuahua", "Vira-lata"
    )

    ProfileStepLayout(step = 4, onBack = onBack) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(text = "Qual a raça dele?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raça") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                racas.filter { it.contains(breed, ignoreCase = true) }.forEach { selection ->
                    DropdownMenuItem(
                        text = { Text(selection) },
                        onClick = {
                            breed = selection
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (breed.isNotBlank()) {
                    UserProfileData.racaCao = breed
                    onFinish()
                } else {
                    Toast.makeText(context, "Por favor, selecione uma raça", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HoofGreenDark)
        ) {
            Text("Concluir", color = Color.White)
        }
    }
}
