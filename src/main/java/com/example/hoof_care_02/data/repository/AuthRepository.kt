package com.example.hoof_care_02.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getUid(): String? = auth.currentUser?.uid

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }


    suspend fun signUp(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user


        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user?.updateProfile(profileUpdates)?.await()
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun updateProfile(name: String? = null, photoUrl: Uri? = null) {
        val user = auth.currentUser ?: return
        val builder = UserProfileChangeRequest.Builder()
        if (name != null) builder.setDisplayName(name)
        if (photoUrl != null) builder.setPhotoUri(photoUrl)
        user.updateProfile(builder.build()).await()
    }

    suspend fun uploadProfilePhoto(localUri: Uri): String {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado.")
        val ref = FirebaseStorage.getInstance().reference.child("users/$uid/profile.jpg")
        ref.putFile(localUri).await()
        val downloadUrl = ref.downloadUrl.await()
        updateProfile(photoUrl = downloadUrl)
        return downloadUrl.toString()
    }

    suspend fun saveUserDescription(description: String) {
        val uid = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(mapOf("descricao" to description), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    suspend fun getUserDescription(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
            snapshot.getString("descricao")
        } catch (e: Exception) {
            null
        }
    }
}