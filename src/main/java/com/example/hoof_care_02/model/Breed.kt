package com.example.hoof_care_02.model

data class Breed(
    val id: String = "",
    val name: String = ""
) {
    override fun toString(): String = name
}

val ALL_BREEDS = listOf(
    Breed("1", "Labrador"),
    Breed("2", "Poodle"),
    Breed("3", "Bulldog"),
    Breed("4", "Golden Retriever"),
    Breed("5", "Pastor Alemão"),
    Breed("6", "Yorkshire"),
    Breed("7", "Beagle"),
    Breed("8", "Shih Tzu"),
    Breed("9", "Chihuahua"),
    Breed("10", "Vira-lata")
)
