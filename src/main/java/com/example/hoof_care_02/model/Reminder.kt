package com.example.hoof_care_02.model

import com.google.firebase.firestore.DocumentId

data class Reminder(
    @DocumentId val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val type: String = "", // Comida, Passeio, Outro
    val time: String = "", // HH:mm
    val title: String? = null,
    val description: String? = null
) {

    fun getDisplayTitle(): String {
        return if (type == "Outro" && !title.isNullOrBlank()) {
            title
        } else {
            type
        }
    }

    fun getDisplayDescription(): String {
        return if (type == "Outro" && !description.isNullOrBlank()) {
            description
        } else {
            "Hora de $type!"
        }
    }
}
