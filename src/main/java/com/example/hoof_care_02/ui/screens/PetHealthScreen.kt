package com.example.hoof_care_02.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.model.Dog
import kotlinx.coroutines.launch

private val HealthGreenDark = Color(0xFF2FAE55)
private val HealthGreenGradientEnd = Color(0xFF3FBE66)
private val HealthBg = Color(0xFFF5F6F7)
private val HealthCardBorder = Color(0xFFE7E9EC)
private val HealthTextMuted = Color(0xFF8A8F98)


private const val NAO_INFORMADO = "Não informado"

data class VetProcedimento(
    val titulo: String,
    val data: String,
    val responsavel: String
)

data class Alergia(
    val nome: String,
    val severidade: String,
    val cor: Color
)

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
    val veterinarioClinica: String
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
        porte = NAO_INFORMADO,
        tipoSanguineo = NAO_INFORMADO,
        pelagem = NAO_INFORMADO,
        castrado = NAO_INFORMADO,
        descricaoLivre = NAO_INFORMADO,
        alergias = emptyList(),
        procedimentos = emptyList(),
        veterinarioNome = NAO_INFORMADO,
        veterinarioClinica = ""
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


data class Vacina(
    val nome: String,
    val dataAplicacao: String,
    val status: StatusVacina
)

enum class StatusVacina(val label: String, val corFundo: Color, val corTexto: Color) {
    EM_DIA("Em dia", Color(0xFFE3F6E8), Color(0xFF2FAE55)),
    VENCENDO("Vencendo", Color(0xFFFFF6DD), Color(0xFFC98A1B)),
    ATRASADA("Atrasada", Color(0xFFFCE8E8), Color(0xFFEF5B5B))
}

private fun exemploVacinas() = listOf(
    Vacina("V10 (Múltipla)", "12/03/2024", StatusVacina.EM_DIA),
    Vacina("Antirrábica", "20/01/2024", StatusVacina.VENCENDO),
    Vacina("Gripe Canina", "05/06/2023", StatusVacina.ATRASADA),
    Vacina("Leishmaniose", "18/09/2024", StatusVacina.EM_DIA),
    Vacina("Giardia", "30/11/2023", StatusVacina.ATRASADA)
)


@Composable
fun PetHealthScreenRoute(
    petId: String,
    onBack: () -> Unit = {},
    onEditarFicha: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var dog by remember { mutableStateOf<Dog?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(petId) {
        scope.launch {
            try {
                dog = com.example.hoof_care_02.data.repository.PetRepository.getDogById(petId)
            } finally {
                isLoading = false
            }
        }
    }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HealthGreenDark)
            }
        }
        dog == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Pet não encontrado.", color = HealthTextMuted)
            }
        }
        else -> {
            PetHealthScreen(
                dog = dog!!,
                onBack = onBack,
                onEditarFicha = onEditarFicha,
                onHomeClick = onHomeClick,
                onProfileClick = onProfileClick
            )
        }
    }
}


@Composable
fun PetHealthScreen(
    dog: Dog,
    onBack: () -> Unit = {},
    onEditarFicha: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val petInfo = remember(dog) { dog.toPetHealthInfo() }
    var selectedTab by remember { mutableStateOf(HealthTab.FICHA_MEDICA) }

    Scaffold(
        containerColor = HealthBg,
        bottomBar = {
            HealthBottomNavBar(onHomeClick = onHomeClick, onProfileClick = onProfileClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            HealthHeader(
                petInfo = petInfo,
                onBack = onBack
            )

            HealthTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            when (selectedTab) {
                HealthTab.FICHA_MEDICA -> FichaMedicaContent(petInfo = petInfo, onEditarFicha = onEditarFicha)
                HealthTab.VACINACAO -> VacinacaoContent()
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
            .background(
                Brush.verticalGradient(
                    listOf(HealthGreenDark, HealthGreenGradientEnd)
                )
            )
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                text = "Saúde",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }) {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = "Favoritar", tint = Color.White)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = Color.White)
                }

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = petInfo.nome,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${petInfo.raca} · ${petInfo.sexo}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Nascido em ${petInfo.nascimento} · ${petInfo.idade}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = petInfo.idExibicao,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Prontuário",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthTabs(
    selectedTab: HealthTab,
    onTabSelected: (HealthTab) -> Unit
) {
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
private fun HealthTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(Color.White)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) HealthGreenDark else HealthTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (selected) HealthGreenDark else HealthTextMuted,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (selected) HealthGreenDark else Color.Transparent)
        )
    }
}

@Composable
private fun FichaMedicaContent(
    petInfo: PetHealthInfo,
    onEditarFicha: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Informações do pet",
                    color = HealthTextMuted,
                    fontSize = 13.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onEditarFicha() }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        tint = HealthGreenDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Editar ficha",
                        color = HealthGreenDark,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(
                    icon = Icons.Filled.Water,
                    label = "Peso",
                    value = petInfo.pesoKg,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    icon = Icons.Filled.Height,
                    label = "Porte",
                    value = petInfo.porte,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    icon = Icons.Filled.FavoriteBorder,
                    label = "Tipo sang.",
                    value = petInfo.tipoSanguineo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SectionCard(title = "Informações Gerais") {
                InfoRow(label = "Raça", value = petInfo.raca)
                InfoRow(label = "Pelagem", value = petInfo.pelagem)
                InfoRow(label = "Castrado", value = petInfo.castrado)
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(text = "Descrição Livre", color = HealthTextMuted, fontSize = 13.sp)
                    Text(
                        text = petInfo.descricaoLivre,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        if (petInfo.alergias.isNotEmpty()) {
            item {
                SectionCard(title = "Alergias Conhecidas") {
                    petInfo.alergias.forEach { alergia ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = alergia.nome, fontSize = 14.sp)
                            SeverityBadge(text = alergia.severidade, color = alergia.cor)
                        }
                    }
                }
            }
        }

        if (petInfo.procedimentos.isNotEmpty()) {
            item {
                SectionCard(title = "Histórico de Procedimentos") {
                    petInfo.procedimentos.forEach { proc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(HealthGreenDark.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = HealthGreenDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(text = proc.titulo, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${proc.data} · ${proc.responsavel}",
                                    color = HealthTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "Veterinário de Confiança") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HealthGreenDark.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val iniciais = petInfo.veterinarioNome
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .joinToString("") { it.first().uppercase() }
                        Text(
                            text = iniciais.ifBlank { "?" },
                            color = HealthGreenDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)
                    ) {
                        Text(text = petInfo.veterinarioNome, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        if (petInfo.veterinarioClinica.isNotBlank()) {
                            Text(text = petInfo.veterinarioClinica, color = HealthTextMuted, fontSize = 12.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HealthGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "Ligar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VacinacaoContent(
    vacinas: List<Vacina> = exemploVacinas(),
    onRegistrarNovaVacina: () -> Unit = {},
    onVacinaClick: (Vacina) -> Unit = {}
) {
    val emDia = vacinas.count { it.status == StatusVacina.EM_DIA }
    val vencendo = vacinas.count { it.status == StatusVacina.VENCENDO }
    val atrasadas = vacinas.count { it.status == StatusVacina.ATRASADA }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VacinaStatusSummaryCard(
                    quantidade = emDia,
                    label = "Em dia",
                    status = StatusVacina.EM_DIA,
                    modifier = Modifier.weight(1f)
                )
                VacinaStatusSummaryCard(
                    quantidade = vencendo,
                    label = "Vencendo",
                    status = StatusVacina.VENCENDO,
                    modifier = Modifier.weight(1f)
                )
                VacinaStatusSummaryCard(
                    quantidade = atrasadas,
                    label = "Atrasada",
                    status = StatusVacina.ATRASADA,
                    modifier = Modifier.weight(1f)
                )
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
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = StatusVacina.ATRASADA.corTexto,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (atrasadas == 1) {
                            "1 vacina atrasada. Agende uma consulta!"
                        } else {
                            "$atrasadas vacinas atrasadas. Agende uma consulta!"
                        },
                        color = StatusVacina.ATRASADA.corTexto,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        items(vacinas) { vacina ->
            VacinaRow(vacina = vacina, onClick = { onVacinaClick(vacina) })
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, HealthGreenDark, RoundedCornerShape(12.dp))
                    .clickable(onClick = onRegistrarNovaVacina)
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = HealthGreenDark,
                    modifier = Modifier.size(18.dp)
                )
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

@Composable
private fun VacinaStatusSummaryCard(
    quantidade: Int,
    label: String,
    status: StatusVacina,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(status.corFundo)
            .border(1.dp, status.corTexto.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$quantidade", color = status.corTexto, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = status.corTexto, fontSize = 12.sp)
    }
}

@Composable
private fun VacinaRow(
    vacina: Vacina,
    onClick: () -> Unit
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
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(HealthGreenDark.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Vaccines, contentDescription = null, tint = HealthGreenDark, modifier = Modifier.size(16.dp))
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(text = vacina.nome, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = "Aplicada em ${vacina.dataAplicacao}", color = HealthTextMuted, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(vacina.status.corFundo)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = vacina.status.label, color = vacina.status.corTexto, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = HealthTextMuted,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(18.dp)
        )
    }
}

@Composable
private fun StatMiniCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthCardBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(HealthGreenDark.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = HealthGreenDark, modifier = Modifier.size(16.dp))
            }
            Text(
                text = label,
                color = HealthTextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HealthCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = HealthTextMuted, fontSize = 14.sp)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SeverityBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HealthBottomNavBar(
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(icon = Icons.Filled.Home, label = "Home", onClick = onHomeClick)

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(HealthGreenDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ContentCut, contentDescription = "Saúde", tint = Color.White)
            }

            NavBarItem(icon = Icons.Filled.Person, label = "Profile", onClick = onProfileClick)
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = HealthTextMuted)
        }
        Text(text = label, color = HealthTextMuted, fontSize = 11.sp)
    }
}