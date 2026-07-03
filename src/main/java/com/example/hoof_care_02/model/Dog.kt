package com.example.hoof_care_02.model

data class Dog(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val sex: String = "",
    val photo: String? = null,
    val birthday: String? = null,
    val weight: Double? = null,
    val breed: Breed = Breed(),
    
    // Campos de Saúde
    val porte: String? = null,
    val tipoSanguineo: String? = null,
    val pelagem: String? = null,
    val castrado: String? = null,
    val descricaoLivre: String? = null,
    val veterinarioNome: String? = null,
    val veterinarioClinica: String? = null,
    val veterinarioTelefone: String? = null
)
