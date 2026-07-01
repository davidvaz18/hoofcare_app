package com.example.hoof_care_02

enum class ActivityType {
    ALIMENTACAO,
    ATIVIDADE_FISICA,
    OUTRO,
    NENHUM
}

data class ActivityCard(
    val id: String,
    val title: String,
    val description: String,
    val type: ActivityType,
    val petName: String
)