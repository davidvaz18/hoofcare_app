package com.example.hoof_care_02.ui.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hoof_care_02.R
import com.example.hoof_care_02.ui.theme.HoofGreenDark
import com.example.hoof_care_02.ui.theme.HoofWhite
import com.example.hoof_care_02.util.SessionManager
import com.example.hoof_care_02.util.UserProfileData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

// Mesmo client e mesma URL do MainActivity2.kt original — NÃO ALTERADO.
private val loginClient = OkHttpClient()
private const val LOGIN_URL = "http://10.0.2.2:8000/api/token/"

/**
 * Equivalente Compose da antiga MainActivity2 (activity_main2.xml).
 * A lógica de rede (fazerLogin) é idêntica à original, apenas adaptada
 * para atualizar estado do Compose em vez de Views/Toast diretos de Activity.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

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
                modifier = Modifier.padding(top = 24.dp)
            )

            RoundedAuthTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = "Senha",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.padding(top = 10.dp)
            )

            PrimaryAuthButton(
                text = "Continue",
                onClick = {
                    val emailTrim = email.trim()
                    val senhaTrim = senha.trim()
                    if (emailTrim.isNotEmpty() && senhaTrim.isNotEmpty()) {
                        fazerLogin(
                            email = emailTrim,
                            password = senhaTrim,
                            context = context,
                            onSuccess = onLoginSuccess
                        )
                    } else {
                        Toast.makeText(context, "Por favor, preencha e-mail e senha.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            )

            OrDivider()

            SocialAuthButton(
                text = "Continuar com o Google",
                iconRes = R.drawable.google_icon_mini,
                onClick = { Log.i("LoginOption", "Google button clicked") }
            )

            SocialAuthButton(
                text = "Continuar com o Facebook",
                iconRes = R.drawable.facebook_icon_mini,
                onClick = { Log.i("LoginOption", "Facebook button clicked") },
                modifier = Modifier.padding(top = 8.dp)
            )

            PrimaryAuthButton(
                text = "Não possui uma conta?",
                onClick = onNavigateToSignIn,
                modifier = Modifier.padding(top = 8.dp)
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

/**
 * Idêntica à fazerLogin() original de MainActivity2.kt:
 * mesma URL, mesmo corpo JSON, mesmo OkHttpClient, mesmo tratamento de resposta.
 * A única diferença é que aqui não há uma Activity para chamar runOnUiThread/Toast/Intent
 * diretamente, então usamos um Handler no main looper e um callback de navegação.
 */
private fun fazerLogin(
    email: String,
    password: String,
    context: android.content.Context,
    onSuccess: () -> Unit
) {
    val json = JSONObject().apply {
        put("email", email)
        put("password", password)
    }

    val requestBody = json.toString().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url(LOGIN_URL)
        .post(requestBody)
        .build()

    loginClient.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val bodyString = response.body?.string()

            if (response.isSuccessful && bodyString != null) {
                val jsonResponse = JSONObject(bodyString)
                val accessToken = jsonResponse.getString("access")
                UserProfileData.nomeUsuario = jsonResponse.optString("username", "Usuário")

                // Salva na memória RAM para uso IMEDIATO
                UserProfileData.accessToken = accessToken

                // Salva no arquivo do dispositivo para o LONGO PRAZO
                SessionManager.saveAuthToken(accessToken)

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Falha no login. Verifique suas credenciais.", Toast.LENGTH_LONG).show()
                }
            }
        }
    })
}
