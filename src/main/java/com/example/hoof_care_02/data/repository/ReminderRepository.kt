package com.example.hoof_care_02.data.repository

import com.example.hoof_care_02.model.Reminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

/**
 * Repositório para gestão de lembretes no Firestore.
 * Caminho: users/{uid}/pets/{petId}/reminders/{reminderId}
 */
object ReminderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getRemindersCollection(petId: String) = 
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .collection("pets").document(petId)
                .collection("reminders")
        }

    /**
     * Busca todos os lembretes de um pet específico.
     */
    suspend fun getReminders(petId: String): List<Reminder> {
        val collection = getRemindersCollection(petId) ?: return emptyList()
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject<Reminder>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Busca todos os lembretes de TODOS os pets do usuário (útil para o BootReceiver).
     */
    suspend fun getAllUserReminders(): List<Reminder> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            // Consulta em coleção de grupo ou percorrendo pets
            // Para simplicidade, vamos buscar através da subcoleção de cada pet
            val pets = PetRepository.getDogs()
            val allReminders = mutableListOf<Reminder>()
            for (pet in pets) {
                allReminders.addAll(getReminders(pet.id))
            }
            allReminders
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveReminder(reminder: Reminder): String? {
        val collection = getRemindersCollection(reminder.petId) ?: return null
        return try {
            if (reminder.id.isEmpty()) {
                val docRef = collection.add(reminder).await()
                docRef.id
            } else {
                collection.document(reminder.id).set(reminder).await()
                reminder.id
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteReminder(petId: String, reminderId: String): Boolean {
        val collection = getRemindersCollection(petId) ?: return false
        return try {
            collection.document(reminderId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
