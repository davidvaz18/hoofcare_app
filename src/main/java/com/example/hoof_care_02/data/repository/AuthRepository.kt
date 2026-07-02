package com.example.hoof_care_02.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositório responsável pela autenticação via Firebase Auth.
 * FirebaseAuth persiste a sessão nativamente no dispositivo.
 */
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

    /**
     * Realiza login com e-mail e senha.
     */
    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    /**
     * Cria uma nova conta e define o nome do usuário.
     */
    suspend fun signUp(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user
        
        // Atualiza o nome de exibição no Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        
        user?.updateProfile(profileUpdates)?.await()
    }

    fun logout() {
        auth.signOut()
    }
}
