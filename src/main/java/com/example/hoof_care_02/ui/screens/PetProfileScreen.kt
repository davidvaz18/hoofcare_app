package com.example.hoof_care_02.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hoof_care_02.R
import com.example.hoof_care_02.data.repository.PetRepository
import com.example.hoof_care_02.model.ALL_BREEDS
import com.example.hoof_care_02.model.Breed
import com.example.hoof_care_02.model.Dog
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    petId: String,
    onBack: () -> Unit,
    onNavigateToHealth: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dog by remember { mutableStateOf<Dog?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    var fieldBeingEdited by remember { mutableStateOf<String?>(null) }
    var editingBreed by remember { mutableStateOf(false) }

    val strPetNotFound = stringResource(R.string.petprofile_not_found)
    val strLoadError = stringResource(R.string.petprofile_load_error)
    val strSaved = stringResource(R.string.petprofile_saved)
    val strSaveError = stringResource(R.string.petprofile_save_error)
    val strNotAuthenticated = stringResource(R.string.petprofile_not_authenticated)
    val strPhotoUpdated = stringResource(R.string.petprofile_photo_updated)
    val strPhotoError = stringResource(R.string.petprofile_photo_error)
    val strInvalidWeight = stringResource(R.string.petprofile_invalid_weight)

    fun reload() {
        scope.launch {
            dog = PetRepository.getDogById(petId)
        }
    }

    LaunchedEffect(petId) {
        scope.launch {
            try {
                dog = PetRepository.getDogById(petId)
                if (dog == null) {
                    Toast.makeText(context, strPetNotFound, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, strLoadError, Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun saveDog(updated: Dog) {
        scope.launch {
            val result = PetRepository.saveDog(updated)
            if (result.isSuccess) {
                dog = updated
                Toast.makeText(context, strSaved, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, strSaveError, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && dog != null) {
            isUploadingPhoto = true
            scope.launch {
                try {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) {
                        Toast.makeText(context, strNotAuthenticated, Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val ref = FirebaseStorage.getInstance().reference
                        .child("users/$uid/pets/${dog!!.id}/photo.jpg")
                    ref.putFile(uri).await()
                    val downloadUrl = ref.downloadUrl.await().toString()
                    val updated = dog!!.copy(photo = downloadUrl)
                    val result = PetRepository.saveDog(updated)
                    if (result.isSuccess) {
                        dog = updated
                        Toast.makeText(context, strPhotoUpdated, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, strPhotoError, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    val msg = context.getString(R.string.petprofile_photo_upload_error, e.message)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.petprofile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHealth) {
                        Icon(Icons.Default.MonitorHeart, contentDescription = stringResource(R.string.petprofile_view_health))
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
                            onClick = { photoPickerLauncher.launch("image/*") },
                            enabled = !isUploadingPhoto,
                            modifier = Modifier.background(HoofGreenDark, CircleShape).size(40.dp)
                        ) {
                            if (isUploadingPhoto) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.petprofile_edit_photo), tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dog!!.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { fieldBeingEdited = "nome" }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.petprofile_edit_name), modifier = Modifier.size(20.dp))
                        }
                    }

                    SuggestionChip(
                        onClick = { editingBreed = true },
                        label = { Text(dog!!.breed.name) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFE8F5E9))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ProfileDetailRow(stringResource(R.string.petprofile_gender), if (dog!!.sex == "M") stringResource(R.string.common_male) else stringResource(R.string.common_female)) {
                                fieldBeingEdited = "genero"
                            }
                            ProfileDetailRowReadOnly(
                                stringResource(R.string.petprofile_age),
                                if (dog!!.age == 1) stringResource(R.string.common_year_1) else stringResource(R.string.common_years, dog!!.age)
                            )
                            ProfileDetailRow(stringResource(R.string.petprofile_weight), stringResource(R.string.common_weight_kg, (dog!!.weight ?: 0.0).toString())) {
                                fieldBeingEdited = "peso"
                            }
                            ProfileDetailRow(
                                stringResource(R.string.petprofile_birthday),
                                formatBirthday(dog!!.birthday, stringResource(R.string.petprofile_birthday_not_informed), stringResource(R.string.petprofile_birthday_display))
                            ) {
                                fieldBeingEdited = "aniversario"
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onNavigateToHealth,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MonitorHeart, contentDescription = stringResource(R.string.petprofile_view_health), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.petprofile_health_button))
                    }
                }
            }

            val currentDog = dog
            if (fieldBeingEdited != null && currentDog != null) {
                when (fieldBeingEdited) {
                    "nome" -> EditTextFieldDialog(
                        title = stringResource(R.string.petprofile_edit_name),
                        label = stringResource(R.string.petprofile_name_label),
                        initialValue = currentDog.name,
                        onDismiss = { fieldBeingEdited = null },
                        onConfirm = { newValue ->
                            saveDog(currentDog.copy(name = newValue))
                            fieldBeingEdited = null
                        }
                    )
                    "genero" -> EditGenderDialog(
                        initialValue = currentDog.sex,
                        onDismiss = { fieldBeingEdited = null },
                        onConfirm = { newValue ->
                            saveDog(currentDog.copy(sex = newValue))
                            fieldBeingEdited = null
                        }
                    )
                    "peso" -> EditTextFieldDialog(
                        title = stringResource(R.string.petprofile_edit_weight),
                        label = stringResource(R.string.petprofile_weight_label),
                        initialValue = currentDog.weight?.toString() ?: "",
                        keyboardType = KeyboardType.Decimal,
                        onDismiss = { fieldBeingEdited = null },
                        onConfirm = { newValue ->
                            val parsed = newValue.replace(",", ".").toDoubleOrNull()
                            if (parsed == null) {
                                Toast.makeText(context, strInvalidWeight, Toast.LENGTH_SHORT).show()
                            } else {
                                saveDog(currentDog.copy(weight = parsed))
                                fieldBeingEdited = null
                            }
                        }
                    )
                    "aniversario" -> EditBirthdayDialog(
                        initialValue = currentDog.birthday,
                        onDismiss = { fieldBeingEdited = null },
                        onConfirm = { newValue ->
                            val idadeRecalculada = calcularIdadeAPartirDoNascimento(newValue)
                            saveDog(currentDog.copy(birthday = newValue, age = idadeRecalculada))
                            fieldBeingEdited = null
                        }
                    )
                }
            }

            if (editingBreed && currentDog != null) {
                EditBreedDialog(
                    currentBreed = currentDog.breed,
                    onDismiss = { editingBreed = false },
                    onConfirm = { newBreed ->
                        saveDog(currentDog.copy(breed = newBreed))
                        editingBreed = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBreedDialog(
    currentBreed: Breed,
    onDismiss: () -> Unit,
    onConfirm: (Breed) -> Unit
) {
    var searchText by remember { mutableStateOf(currentBreed.name) }
    var selectedBreed by remember { mutableStateOf(currentBreed) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.petprofile_edit_breed)) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            selectedBreed = ALL_BREEDS.firstOrNull { b -> b.name.equals(it, ignoreCase = true) } ?: selectedBreed
                        },
                        label = { Text(stringResource(R.string.petprofile_breed_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    val filtered = ALL_BREEDS.filter { it.name.contains(searchText, ignoreCase = true) }
                    ExposedDropdownMenu(
                        expanded = expanded && filtered.isNotEmpty(),
                        onDismissRequest = { expanded = false }
                    ) {
                        filtered.forEach { breed ->
                            DropdownMenuItem(
                                text = { Text(breed.name) },
                                onClick = {
                                    selectedBreed = breed
                                    searchText = breed.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                if (selectedBreed.id.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.petprofile_breed_recognized),
                        color = Color(0xFF38C075),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedBreed.id.isNotBlank()) onConfirm(selectedBreed)
                },
                enabled = selectedBreed.id.isNotBlank()
            ) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@Composable
fun ProfileDetailRowReadOnly(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Medium)
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
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.common_edit), modifier = Modifier.size(18.dp), tint = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextFieldDialog(
    title: String,
    label: String,
    initialValue: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (value.isBlank()) return@TextButton
                onConfirm(value.trim())
            }) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@Composable
fun EditGenderDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selected by remember { mutableStateOf(if (initialValue == "F") "F" else "M") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.petprofile_edit_gender)) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selected = "M" }) {
                    RadioButton(selected = selected == "M", onClick = { selected = "M" })
                    Text(stringResource(R.string.common_male))
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selected = "F" }) {
                    RadioButton(selected = selected == "F", onClick = { selected = "F" })
                    Text(stringResource(R.string.common_female))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@Composable
fun EditBirthdayDialog(
    initialValue: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    if (!initialValue.isNullOrBlank()) {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(initialValue)?.let { calendar.time = it }
        } catch (_: Exception) { }
    }

    LaunchedEffect(Unit) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                val formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                onConfirm(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { onDismiss() }
            show()
        }
    }
}

private fun formatBirthday(birthday: String?, notInformedText: String, displayPattern: String): String {
    if (birthday == null) return notInformedText
    return try {
        val backendFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat(displayPattern, Locale("pt", "BR"))
        val date = backendFormat.parse(birthday)
        if (date != null) displayFormat.format(date) else birthday
    } catch (e: Exception) {
        birthday
    }
}
