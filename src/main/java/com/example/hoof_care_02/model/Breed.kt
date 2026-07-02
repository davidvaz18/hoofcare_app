package com.example.hoof_care_02.model

data class Breed(
    val id: String = "",
    val name: String = ""
) {
    override fun toString(): String = name
}
