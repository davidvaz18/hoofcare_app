package com.example.hoof_care_02.model

data class Breed(
    val id: Int,
    val name: String
) {
    // Isso faz com que o nome da raça apareça no Spinner
    override fun toString(): String {
        return name
    }
}