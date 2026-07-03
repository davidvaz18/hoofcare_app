package com.example.hoof_care_02.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.data.repository.PetRepository
import com.example.hoof_care_02.model.*
import kotlinx.coroutines.launch
import java.util.*

private val HealthGreenDark = Color(0xFF2E7D32)
private val HealthGreenGradientEnd = Color(0xFF3FBE66)
private val HealthBg = Color(0xFFF5F6F7)
private val HealthCardBorder = Color(0xFFE7E9EC)
private val HealthTextMuted = Color(0xFF6C757D)

private const val NAO_INFORMADO = "Não informado"

data class PetHealthInfo(
    val nome: String,
    val raca: String,
    val sexo: String,
    val nascimento: String,
    val idade: String,
    val idExibicao: String,
    val pesoKg: String,
    val porte: String,
    val tipoSanguineo: String,
    val pelagem: String,
    val castrado: String,
    val descricaoLivre: String,
    val alergias: List<Alergia>,
    val procedimentos: List<VetProcedimento>,
    val veterinarioNome: String,
    val veterinarioClinica: String,
    val veterinarioTelefone: String
)

fun Dog.toPetHealthInfo(): PetHealthInfo {
    val sexoExibido = when (sex) {
        "M" -> "Macho"
        "F" -> "Fêmea"
        else -> if (sex.isBlank()) NAO_INFORMADO else sex
    }

    val nascimentoExibido = birthday
        ?.takeIf { it.isNotBlank() }
        ?.let { formatarDataIsoParaBr(it) }
        ?: NAO_INFORMADO

    val idadeExibida = if (age > 0) {
        if (age == 1) "1 ano" else "$age anos"
    } else {
        NAO_INFORMADO
    }

    val pesoExibido = if (weight != null && weight > 0.0) "$weight kg" else NAO_INFORMADO

    return PetHealthInfo(
        nome = name,
        raca = breed.name,
        sexo = sexoExibido,
        nascimento = nascimentoExibido,
        idade = idadeExibida,
        idExibicao = "ID #$id",
        pesoKg = pesoExibido,
        porte = porte?.takeIf { it.isNotBlank() } ?: NAO_INFORMADO,
        tipoSanguineo = tipoSanguineo?.takeIf { it.isNotBlank() } ?: NAO_INFORMADO,
        pelagem = pelagem?.takeIf { it.isNotBlank() } ?: NAO_INFORMADO,
        castrado = castrado?.takeIf { it.isNotBlank() } ?: NAO_INFORMADO,
        descricaoLivre = descricaoLivre?.takeIf { it.isNotBlank() } ?: NAO_INFORMADO,
        alergias = emptyList(),
        procedimentos = emptyList(),
        veterinarioNome = veterinarioNome?.takeIf { it.isNotBlank() } ?: NAO_INFORMADO,
        veterinarioClinica = veterinarioClinica ?: "",
        veterinarioTelefone = veterinarioTelefone ?: ""
    )
}

private fun formatarDataIsoParaBr(dataIso: String): String {
    val partes = dataIso.split("-")
    return if (partes.size == 3) {
        "${partes[2]}/${partes[1]}/${partes[0]}"
    } else {
        dataIso
    }
}

@Composable
fun PetHealthScreenRoute(
    petId: String,
    onBack: () -> Unit = {},
    onEditarFicha: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onPetsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var dog by remember { mutableStateOf<Dog?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshData() {
        scope.launch {
            try {
                dog = PetRepository.getDogById(petId)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(petId) { refreshData() }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HealthGreenDark)
        }
    } else if (dog == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pet não encontrado.", color = HealthTextMuted)
        }
    } else {
        PetHealthScreen(
            dog = dog!!,
            onBack = onBack,
            onEditarFicha = onEditarFicha,
            onHomeClick = onHomeClick,
            onPetsClick = onPetsClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun PetHealthScreen(
    dog: Dog,
    onBack: () -> Unit = {},
    onEditarFicha: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onPetsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val petInfo = remember(dog) { dog.toPetHealthInfo() }
    var selectedTab by remember { mutableStateOf(HealthTab.FICHA_MEDICA) }

    Scaffold(
        containerColor = HealthBg,
        bottomBar = {
            // FIX: Adicionado navigationBarsPadding() para evitar que a nav bar do celular sobreponha a do app
            Box(modifier = Modifier.navigationBarsPadding()) {
                AppBottomNavigationBar(
                    selectedTab = BottomNavTab.PETS,
                    onHomeClick = onHomeClick,
                    onPetsClick = onPetsClick,
                    onProfileClick = onProfileClick,
                    middleIsHealth = true
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            HealthHeader(petInfo = petInfo, onBack = onBack)

            HealthTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            when (selectedTab) {
                HealthTab.FICHA_MEDICA -> FichaMedicaContent(
                    petInfo = petInfo,
                    onEditarFicha = onEditarFicha,
                    onLigarVeterinario = {
                        val telefone = petInfo.veterinarioTelefone
                        if (telefone.isNotBlank()) {
                            try {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefone")))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Não foi possível abrir o discador.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Nenhum telefone cadastrado.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                HealthTab.VACINACAO -> VacinacaoContent(petId = dog.id)
            }
        }
    }
}

private enum class HealthTab { FICHA_MEDICA, VACINACAO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthHeader(
    petInfo: PetHealthInfo,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(HealthGreenDark, HealthGreenGradientEnd)))
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text("Saúde", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Pets, contentDescription = "Ícone do pet", tint = Color.White)
                }
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(petInfo.nome, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${petInfo.raca} · ${petInfo.sexo}", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("Nascido em ${petInfo.nascimento} · ${petInfo.idade}", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(petInfo.idExibicao, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Filled.Description, contentDescription = "Prontuário", tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("Prontuário", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthTabs(selectedTab: HealthTab, onTabSelected: (HealthTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        HealthTabItem(
            label = "Ficha Médica",
            icon = Icons.Filled.Description,
            selected = selectedTab == HealthTab.FICHA_MEDICA,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(HealthTab.FICHA_MEDICA) }
        )
        HealthTabItem(
            label = "Vacinação",
            icon = Icons.Filled.Vaccines,
            selected = selectedTab == HealthTab.VACINACAO,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(HealthTab.VACINACAO) }
        )
    }
}

@Composable
private fun HealthTabItem(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.background(Color.White).clickable { onClick() }.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = if (selected) HealthGreenDark else HealthTextMuted, modifier = Modifier.size(16.dp))
            Text(label, color = if (selected) HealthGreenDark else HealthTextMuted, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.padding(start = 6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(if (selected) HealthGreenDark else Color.Transparent))
    }
}

@Composable
private fun FichaMedicaContent(
    petInfo: PetHealthInfo,
    onEditarFicha: () -> Unit,
    onLigarVeterinario: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Informações do pet", color = HealthTextMuted, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onEditarFicha() }.padding(vertical = 4.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar ficha", tint = HealthGreenDark, modifier = Modifier.size(16.dp))
                    Text("Editar ficha", color = HealthGreenDark, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMiniCard(icon = Icons.Filled.Water, label = "Peso", value = petInfo.pesoKg, modifier = Modifier.weight(1f))
                StatMiniCard(icon = Icons.Filled.Height, label = "Porte", value = petInfo.porte, modifier = Modifier.weight(1f))
                StatMiniCard(icon = Icons.Filled.FavoriteBorder, label = "Tipo sang.", value = petInfo.tipoSanguineo, modifier = Modifier.weight(1f))
            }
        }
        item {
            SectionCard(title = "Informações Gerais") {
                InfoRow(label = "Raça", value = petInfo.raca)
                InfoRow(label = "Pelagem", value = petInfo.pelagem)
                InfoRow(label = "Castrado", value = petInfo.castrado)
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("Descrição Livre", color = HealthTextMuted, fontSize = 13.sp)
                    Text(petInfo.descricaoLivre, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        item {
            SectionCard(title = "Veterinário de Confiança") {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(HealthGreenDark.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        val iniciais = petInfo.veterinarioNome.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
                        Text(iniciais.ifBlank { "?" }, color = HealthGreenDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(petInfo.veterinarioNome, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        if (petInfo.veterinarioClinica.isNotBlank()) Text(petInfo.veterinarioClinica, color = HealthTextMuted, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HealthGreenDark)
                            .clickable { onLigarVeterinario() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Call, "Ligar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VacinacaoContent(petId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var vacinas by remember { mutableStateOf<List<Vacina>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showFormDialog by remember { mutableStateOf(false) }
    var vacinaBeingEdited by remember { mutableStateOf<Vacina?>(null) }
    var vacinaPendingDelete by remember { mutableStateOf<Vacina?>(null) }

    fun loadVacinas() {
        scope.launch {
            isLoading = true
            vacinas = PetRepository.getVacinas(petId)
            isLoading = false
        }
    }

    LaunchedEffect(petId) { loadVacinas() }

    val emDia = vacinas.count { it.status == StatusVacina.EM_DIA }
    val vencendo = vacinas.count { it.status == StatusVacina.VENCENDO }
    val atrasadas = vacinas.count { it.status == StatusVacina.ATRASADA }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = HealthGreenDark)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VacinaStatusSummaryCard(emDia, "Em dia", StatusVacina.EM_DIA, Modifier.weight(1f))
                        VacinaStatusSummaryCard(vencendo, "Vencendo", StatusVacina.VENCENDO, Modifier.weight(1f))
                        VacinaStatusSummaryCard(atrasadas, "Atrasada", StatusVacina.ATRASADA, Modifier.weight(1f))
                    }
                }

                if (atrasadas > 0) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusVacina.ATRASADA.corFundo)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = StatusVacina.ATRASADA.corTexto, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (atrasadas == 1) "1 vacina atrasada. Agende uma consulta!" else "$atrasadas vacinas atrasadas. Agende uma consulta!",
                                color = StatusVacina.ATRASADA.corTexto,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                if (vacinas.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma vacina registrada ainda.",
                            color = HealthTextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                items(vacinas, key = { it.id }) { vacina ->
                    VacinaRow(
                        vacina = vacina,
                        onClick = {
                            vacinaBeingEdited = vacina
                            showFormDialog = true
                        },
                        onDelete = { vacinaPendingDelete = vacina }
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, HealthGreenDark, RoundedCornerShape(12.dp))
                            .clickable {
                                vacinaBeingEdited = null
                                showFormDialog = true
                            }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = HealthGreenDark, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Registrar nova vacina",
                            color = HealthGreenDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }

        if (showFormDialog) {
            VacinaFormDialog(
                existingVacina = vacinaBeingEdited,
                onDismiss = {
                    showFormDialog = false
                    vacinaBeingEdited = null
                },
                onSave = { vacina ->
                    scope.launch {
                        val result = PetRepository.saveVacina(petId, vacina)
                        if (result.isSuccess) {
                            showFormDialog = false
                            vacinaBeingEdited = null
                            loadVacinas()
                        } else {
                            Toast.makeText(context, "Erro ao salvar vacina: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }

        val toDelete = vacinaPendingDelete
        if (toDelete != null) {
            AlertDialog(
                onDismissRequest = { vacinaPendingDelete = null },
                title = { Text("Excluir vacina") },
                text = { Text("Tem certeza que deseja excluir \"${toDelete.nome}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            val result = PetRepository.deleteVacina(petId, toDelete.id)
                            if (result.isSuccess) {
                                vacinaPendingDelete = null
                                loadVacinas()
                            } else {
                                Toast.makeText(context, "Erro ao excluir vacina.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Excluir", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { vacinaPendingDelete = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VacinaFormDialog(
    existingVacina: Vacina?,
    onDismiss: () -> Unit,
    onSave: (Vacina) -> Unit
) {
    val context = LocalContext.current
    val isEditing = existingVacina != null
    var nome by remember { mutableStateOf(existingVacina?.nome ?: "") }
    var dataAplicacao by remember { mutableStateOf(existingVacina?.dataAplicacao ?: "") }
    var status by remember { mutableStateOf(existingVacina?.status ?: StatusVacina.EM_DIA) }
    var statusExpanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                dataAplicacao = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Vacina" else "Registrar Vacina") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da vacina") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dataAplicacao.ifBlank { "Data de aplicação" })
                }

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = status.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        StatusVacina.entries.forEach {
                            DropdownMenuItem(text = { Text(it.label) }, onClick = { status = it; statusExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nome.isBlank() || dataAplicacao.isBlank()) {
                    Toast.makeText(context, "Preencha nome e data.", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                onSave(
                    Vacina(
                        id = existingVacina?.id ?: "",
                        nome = nome.trim(),
                        dataAplicacao = dataAplicacao,
                        status = status
                    )
                )
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun VacinaStatusSummaryCard(quantidade: Int, label: String, status: StatusVacina, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(status.corFundo).border(1.dp, status.corTexto.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$quantidade", color = status.corTexto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = status.corTexto, fontSize = 12.sp)
    }
}

@Composable
private fun VacinaRow(
    vacina: Vacina,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, HealthCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(HealthGreenDark.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { 
            Icon(Icons.Filled.Vaccines, contentDescription = "Vacina", tint = HealthGreenDark, modifier = Modifier.size(16.dp)) 
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(vacina.nome, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("Aplicada em ${vacina.dataAplicacao}", color = HealthTextMuted, fontSize = 12.sp)
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(vacina.status.corFundo).padding(horizontal = 10.dp, vertical = 4.dp)) { 
            Text(vacina.status.label, color = vacina.status.corTexto, fontSize = 12.sp, fontWeight = FontWeight.Medium) 
        }
        
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Excluir vacina", tint = HealthTextMuted, modifier = Modifier.size(18.dp))
        }

        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Detalhes", tint = HealthTextMuted, modifier = Modifier.padding(start = 2.dp).size(18.dp))
    }
}

@Composable
private fun StatMiniCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, HealthCardBorder)) {
        Column(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(HealthGreenDark.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = HealthGreenDark, modifier = Modifier.size(16.dp)) }
            Text(label, color = HealthTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, HealthCardBorder)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = HealthTextMuted, fontSize = 14.sp)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SeverityBadge(text: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}