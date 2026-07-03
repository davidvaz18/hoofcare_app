package com.example.hoof_care_02.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hoof_care_02.R
import com.example.hoof_care_02.data.repository.AuthRepository
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.ui.theme.HoofWhite
import com.example.hoof_care_02.util.UserProfileData
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HoofGreenDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.hoofcare),
                contentDescription = null,
                modifier = Modifier
                    .width(300.dp)
                    .height(180.dp)
            )

            Text(
                text = "Entre com a sua conta\n Digite seu email para se entrar nesse app.",
                color = HoofWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            RoundedAuthTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "email@exemplo.com",
                keyboardType = KeyboardType.Email,
                modifier = Modifier.padding(top = 24.dp),
                enabled = !isLoading
            )

            RoundedAuthTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = "Senha",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.padding(top = 10.dp),
                enabled = !isLoading
            )

            if (isLoading) {
                CircularProgressIndicator(color = HoofWhite, modifier = Modifier.padding(top = 16.dp))
            } else {
                PrimaryAuthButton(
                    text = "Continue",
                    onClick = {
                        val emailTrim = email.trim()
                        val senhaTrim = senha.trim()
                        if (emailTrim.isNotEmpty() && senhaTrim.isNotEmpty()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    AuthRepository.signIn(emailTrim, senhaTrim)
                                    val user = AuthRepository.currentUser.value
                                    UserProfileData.nomeUsuario = user?.displayName ?: "Usuário"
                                    
                                    Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    Toast.makeText(context, "Usuário não encontrado.", Toast.LENGTH_LONG).show()
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(context, "E-mail ou senha incorretos.", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro ao fazer login: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Por favor, preencha e-mail e senha.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            OrDivider()

            SocialAuthButton(
                text = "Continuar com o Google",
                iconRes = R.drawable.google_icon_mini,
                onClick = { /* Implementação futura */ },
                enabled = !isLoading
            )

            SocialAuthButton(
                text = "Continuar com o Facebook",
                iconRes = R.drawable.facebook_icon_mini,
                onClick = { /* Implementação futura */ },
                modifier = Modifier.padding(top = 8.dp),
                enabled = !isLoading
            )

            PrimaryAuthButton(
                text = "Não possui uma conta?",
                onClick = onNavigateToSignIn,
                modifier = Modifier.padding(top = 8.dp),
                enabled = !isLoading
            )

            Text(
                text = "Ao clicar em continuar, você concorda com nossos Termos de Serviço e Política de Privacidade.",
                color = HoofWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}
