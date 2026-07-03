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
    var emailError by remember { mutableStateOf<String?>(null) }
    var senhaError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val strEmailRequired = stringResource(R.string.auth_error_email_required)
    val strInvalidEmail = stringResource(R.string.auth_error_invalid_email)
    val strPasswordRequired = stringResource(R.string.auth_error_password_required)
    val strGenericUser = stringResource(R.string.common_username_fallback)
    val strLoginSuccess = stringResource(R.string.auth_login_success)
    val strUserNotFound = stringResource(R.string.auth_user_not_found)
    val strWrongCred = stringResource(R.string.auth_wrong_credentials)

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
                text = stringResource(R.string.auth_login_title),
                color = HoofWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            RoundedAuthTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                placeholder = stringResource(R.string.auth_email_placeholder),
                keyboardType = KeyboardType.Email,
                modifier = Modifier.padding(top = 24.dp),
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

            if (isLoading) {
                CircularProgressIndicator(color = HoofWhite, modifier = Modifier.padding(top = 16.dp))
            } else {
                PrimaryAuthButton(
                    text = stringResource(R.string.auth_login_button),
                    onClick = {
                        val emailTrim = email.trim()
                        val senhaTrim = senha.trim()
                        emailError = when {
                            emailTrim.isEmpty() -> strEmailRequired
                            !emailTrim.contains("@") -> strInvalidEmail
                            else -> null
                        }
                        senhaError = if (senhaTrim.isEmpty()) strPasswordRequired else null
                        if (emailError != null || senhaError != null) return@PrimaryAuthButton

                        isLoading = true
                        scope.launch {
                            try {
                                AuthRepository.signIn(emailTrim, senhaTrim)
                                val user = AuthRepository.currentUser.value
                                UserProfileData.nomeUsuario = user?.displayName ?: strGenericUser
                                
                                Toast.makeText(context, strLoginSuccess, Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } catch (e: FirebaseAuthInvalidUserException) {
                                Toast.makeText(context, strUserNotFound, Toast.LENGTH_LONG).show()
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(context, strWrongCred, Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                val msg = context.getString(R.string.auth_login_error, e.localizedMessage ?: "")
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            PrimaryAuthButton(
                text = stringResource(R.string.auth_no_account),
                onClick = onNavigateToSignIn,
                modifier = Modifier.padding(top = 8.dp),
                enabled = !isLoading
            )

            Text(
                text = stringResource(R.string.auth_terms),
                color = HoofWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}
