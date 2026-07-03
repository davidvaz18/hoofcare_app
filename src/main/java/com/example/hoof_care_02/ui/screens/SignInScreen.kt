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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hoof_care_02.R
import com.example.hoof_care_02.data.repository.AuthRepository
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.ui.theme.HoofWhite
import com.example.hoof_care_02.util.UserProfileData
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.launch


@Composable
fun SignInScreen(
    onSignUpSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var nomeError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var senhaError by remember { mutableStateOf<String?>(null) }
    var confirmarSenhaError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val strNameRequired = stringResource(R.string.auth_error_name_required)
    val strEmailRequired = stringResource(R.string.auth_error_email_required)
    val strInvalidEmail = stringResource(R.string.auth_error_invalid_email)
    val strPasswordRequired = stringResource(R.string.auth_error_password_required)
    val strConfirmRequired = stringResource(R.string.auth_error_confirm_password_required)
    val strPassMismatch = stringResource(R.string.auth_password_mismatch)
    val strSignupSuccess = stringResource(R.string.auth_signup_success)
    val strWeakPass = stringResource(R.string.auth_weak_password)
    val strInvalidEmailMsg = stringResource(R.string.auth_invalid_email)
    val strEmailInUse = stringResource(R.string.auth_email_in_use)

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
                contentDescription = stringResource(R.string.auth_logo_description),
                modifier = Modifier
                    .width(300.dp)
                    .height(180.dp)
            )

            Text(
                text = stringResource(R.string.auth_signup_title),
                color = HoofWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            RoundedAuthTextField(
                value = nome,
                onValueChange = { nome = it; nomeError = null },
                placeholder = stringResource(R.string.auth_name_placeholder),
                modifier = Modifier.padding(top = 4.dp),
                enabled = !isLoading,
                isError = nomeError != null,
                supportingText = nomeError
            )

            RoundedAuthTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                placeholder = stringResource(R.string.auth_email_placeholder),
                keyboardType = KeyboardType.Email,
                modifier = Modifier.padding(top = 10.dp),
                enabled = !isLoading,
                isError = emailError != null,
                supportingText = emailError
            )

            RoundedAuthTextField(
                value = senha,
                onValueChange = { senha = it; senhaError = null },
                placeholder = stringResource(R.string.auth_password_placeholder),
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.padding(top = 10.dp),
                enabled = !isLoading,
                isError = senhaError != null,
                supportingText = senhaError
            )

            RoundedAuthTextField(
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it; confirmarSenhaError = null },
                placeholder = stringResource(R.string.auth_confirm_password_placeholder),
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.padding(top = 10.dp),
                enabled = !isLoading,
                isError = confirmarSenhaError != null,
                supportingText = confirmarSenhaError
            )

            if (isLoading) {
                CircularProgressIndicator(color = HoofWhite, modifier = Modifier.padding(top = 16.dp))
            } else {
                PrimaryAuthButton(
                    text = stringResource(R.string.auth_signup_button),
                    onClick = {
                        val nomeTrim = nome.trim()
                        val emailTrim = email.trim()
                        val senhaTrim = senha.trim()
                        val confirmarSenhaTrim = confirmarSenha.trim()

                        nomeError = if (nomeTrim.isEmpty()) strNameRequired else null
                        emailError = when {
                            emailTrim.isEmpty() -> strEmailRequired
                            !emailTrim.contains("@") -> strInvalidEmail
                            else -> null
                        }
                        senhaError = if (senhaTrim.isEmpty()) strPasswordRequired else null
                        confirmarSenhaError = if (confirmarSenhaTrim.isEmpty()) strConfirmRequired else null

                        if (nomeError != null || emailError != null || senhaError != null || confirmarSenhaError != null) {
                            return@PrimaryAuthButton
                        }

                        if (senhaTrim != confirmarSenhaTrim) {
                            confirmarSenhaError = strPassMismatch
                            return@PrimaryAuthButton
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                AuthRepository.signUp(nomeTrim, emailTrim, senhaTrim)
                                UserProfileData.nomeUsuario = nomeTrim
                                
                                Toast.makeText(context, strSignupSuccess, Toast.LENGTH_SHORT).show()
                                onSignUpSuccess()
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                Toast.makeText(context, strWeakPass, Toast.LENGTH_LONG).show()
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(context, strInvalidEmailMsg, Toast.LENGTH_LONG).show()
                            } catch (e: FirebaseAuthUserCollisionException) {
                                Toast.makeText(context, strEmailInUse, Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                val msg = context.getString(R.string.auth_signup_error, e.localizedMessage ?: "")
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

        }
    }
}
