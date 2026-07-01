package com.example.hoof_care_02.model

data class Dog(
    val id: Int,
    val name: String,
    val age: Int,
    val sex: String,
    val photo: String?, // A URL da foto pode ser nula
    val birthday: String?,
    val weight: Double?,
    val breed: Breed // Um objeto Breed aninhado
)