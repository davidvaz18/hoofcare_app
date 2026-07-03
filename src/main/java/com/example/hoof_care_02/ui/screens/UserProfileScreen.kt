package com.example.hoof_care_02.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
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
import com.example.hoof_care_02.data.repository.AuthRepository
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var userName by remember { mutableStateOf(currentUser?.displayName ?: com.example.hoof_care_02.util.UserProfileData.nomeUsuario ?: "Usuário") }
    var userDescription by remember { mutableStateOf(com.example.hoof_care_02.util.UserProfileData.descricaoUsuario ?: "") }
    var photoUrl by remember { mutableStateOf(currentUser?.photoUrl?.toString()) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val savedDescription = AuthRepository.getUserDescription()
        if (!savedDescription.isNullOrBlank()) {
            userDescription = savedDescription
            com.example.hoof_care_02.util.UserProfileData.descricaoUsuario = savedDescription
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingPhoto = true
            scope.launch {
                try {
                    val newUrl = AuthRepository.uploadProfilePhoto(uri)
                    photoUrl = newUrl
                    Toast.makeText(context, "Foto atualizada.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao enviar foto: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Color.Red)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(contentAlignment = Alignment.BottomEnd) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.fotousuario),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5))
                    )
                }
                IconButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    enabled = !isUploadingPhoto,
                    modifier = Modifier
                        .background(HoofGreenDark, CircleShape)
                        .size(36.dp)
                ) {
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileInfoRow(label = "Nome", value = userName)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
                    ProfileInfoRow(label = "Sobre mim", value = userDescription.ifBlank { "Nenhuma descrição adicionada" })
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HoofGreenDark)
            ) {
                Text("Editar Perfil", fontSize = 18.sp, color = Color.White)
            }
        }

        if (showEditDialog) {
            EditUserProfileDialog(
                initialName = userName,
                initialDescription = userDescription,
                onDismiss = { showEditDialog = false },
                onConfirm = { newName, newDescription ->
                    scope.launch {
                        try {
                            AuthRepository.updateProfile(name = newName)
                            AuthRepository.saveUserDescription(newDescription)
                            userName = newName
                            userDescription = newDescription
                            com.example.hoof_care_02.util.UserProfileData.nomeUsuario = newName
                            com.example.hoof_care_02.util.UserProfileData.descricaoUsuario = newDescription
                            showEditDialog = false
                            Toast.makeText(context, "Perfil atualizado.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao salvar perfil.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileDialog(
    initialName: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Sobre mim") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) return@TextButton
                onConfirm(name.trim(), description.trim())
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}