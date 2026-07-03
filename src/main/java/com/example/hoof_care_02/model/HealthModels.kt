package com.example.hoof_care_02.model

import androidx.compose.ui.graphics.Color

data class VetProcedimento(
    val titulo: String = "",
    val data: String = "",
    val responsavel: String = ""
)

data class Alergia(
    val nome: String = "",
    val severidade: String = "", // "Moderada" | "Grave" | "Leve"
    val corHex: String = "#000000"
) {
    val cor: Color get() = try { Color(android.graphics.Color.parseColor(corHex)) } catch (e: Exception) { Color.Black }
}

data class Vacina(
    val id: String = "",
    val nome: String = "",
    val dataAplicacao: String = "",
    val status: StatusVacina = StatusVacina.EM_DIA
)

enum class StatusVacina(val label: String) {
    EM_DIA("Em dia"),
    VENCENDO("Vencendo"),
    ATRASADA("Atrasada");

    val corFundo: Color get() = when(this) {
        EM_DIA -> Color(0xFFE3F6E8)
        VENCENDO -> Color(0xFFFFF6DD)
        ATRASADA -> Color(0xFFFCE8E8)
    }

    val corTexto: Color get() = when(this) {
        EM_DIA -> Color(0xFF1E7A3A)
        VENCENDO -> Color(0xFF9A6A00)
        ATRASADA -> Color(0xFFC62828)
    }
}
