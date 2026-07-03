package com.example.hoof_care_02.data.repository

import com.example.hoof_care_02.model.*
import com.example.hoof_care_02.ui.screens.Alergia
import com.example.hoof_care_02.ui.screens.Vacina
import com.example.hoof_care_02.ui.screens.VetProcedimento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

object PetRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val petsCollection
        get() = auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("pets")
        }

    suspend fun getDogs(): List<Dog> {
        val collection = petsCollection ?: return emptyList()
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject<Dog>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDogById(petId: String): Dog? {
        val collection = petsCollection ?: return null
        return try {
            collection.document(petId).get().await().toObject<Dog>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveDog(dog: Dog): Result<Unit> {
        val collection = petsCollection
            ?: return Result.failure(Exception("Usuário não autenticado."))

        return try {
            if (dog.id.isEmpty()) {
                val docRef = collection.document()
                val dogWithId = dog.copy(id = docRef.id)
                docRef.set(dogWithId).await()
            } else {
                collection.document(dog.id).set(dog).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDog(petId: String): Result<Unit> {
        val collection = petsCollection
            ?: return Result.failure(Exception("Usuário não autenticado."))

        return try {
            collection.document(petId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVacinas(petId: String): List<Vacina> {
        val collection = petsCollection?.document(petId)?.collection("vacinas") ?: return emptyList()
        return try {
            collection.get().await().documents.mapNotNull { it.toObject<Vacina>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAlergias(petId: String): List<Alergia> {
        val collection = petsCollection?.document(petId)?.collection("alergias") ?: return emptyList()
        return try {
            collection.get().await().documents.mapNotNull { it.toObject<Alergia>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProcedimentos(petId: String): List<VetProcedimento> {
        val collection = petsCollection?.document(petId)?.collection("procedimentos") ?: return emptyList()
        return try {
            collection.get().await().documents.mapNotNull { it.toObject<VetProcedimento>() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}