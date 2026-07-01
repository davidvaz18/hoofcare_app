package com.example.hoof_care_02.ui.screens

import android.os.Handler
import android.os.Looper
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

// Mesmo client e mesma URL do SignInActivity.kt original — NÃO ALTERADO.
private val signInClient = OkHttpClient()
private const val SIGN_IN_API_URL = "http://10.0.2.2:8000/api/register/"

/**
 * Equivalente Compose da antiga SignInActivity (activity_sign_in.xml).
 * A lógica de rede (enviarDadosParaApi) é idêntica à original, apenas adaptada
 * para Compose state + callback de navegação em vez de Intent direto.
 */
@Composable
fun SignInScreen(
    onSignUpSuccess: () -> Unit
) {
    val context = LocalContext.current
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }

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
                text = "Crie sua conta\n Digite seu email para se cadastrar nesse app.",
                color = HoofWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            RoundedAuthTextField(
                value = nome,
                onValueChange = { nome = it },
                placeholder = "Nome",
                modifier = Modifier.padding(top = 4.dp)
            )

            RoundedAuthTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "email@exemplo.com",
                keyboardType = KeyboardType.Email,
                modifier = Modifier.padding(top = 10.dp)
            )

            RoundedAuthTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = "Senha",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.padding(top = 10.dp)
            )

            RoundedAuthTextField(
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it },
                placeholder = "Confirmar Senha",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.padding(top = 10.dp)
            )

            PrimaryAuthButton(
                text = "Continuar",
                onClick = {
                    val nomeTrim = nome.trim()
                    val emailTrim = email.trim()
                    val senhaTrim = senha.trim()
                    val confirmarSenhaTrim = confirmarSenha.trim()

                    if (nomeTrim.isNotEmpty() && emailTrim.isNotEmpty() && senhaTrim.isNotEmpty() && confirmarSenhaTrim.isNotEmpty()) {
                        UserProfileData.nomeUsuario = nomeTrim
                        enviarDadosParaApi(
                            nome = nomeTrim,
                            email = emailTrim,
                            senha = senhaTrim,
                            confirmarSenha = confirmarSenhaTrim,
                            context = context,
                            onSuccess = onSignUpSuccess
                        )
                    } else {
                        Toast.makeText(context, "Preencha todos os campos antes de continuar.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            )

            OrDivider()

            SocialAuthButton(
                text = "Continuar com o Google",
                iconRes = R.drawable.google_icon_mini,
                onClick = { }
            )

            SocialAuthButton(
                text = "Continuar com o Facebook",
                iconRes = R.drawable.facebook_icon_mini,
                onClick = { },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Idêntica à enviarDadosParaApi() original de SignInActivity.kt:
 * mesma URL, mesmo corpo JSON, mesmo OkHttpClient, mesmo tratamento de resposta.
 * Após sucesso, navega para UserProfileActivity01 (próxima etapa da migração).
 */
private fun enviarDadosParaApi(
    nome: String,
    email: String,
    senha: String,
    confirmarSenha: String,
    context: android.content.Context,
    onSuccess: () -> Unit
) {
    val json = JSONObject().apply {
        put("username", nome)
        put("email", email)
        put("password", senha)
        put("password2", confirmarSenha)
    }

    val requestBody = json.toString().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url(SIGN_IN_API_URL)
        .post(requestBody)
        .build()

    signInClient.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val isSuccessful = response.isSuccessful
            val errorBody = if (!isSuccessful) response.body?.string() ?: "Erro desconhecido" else null

            Handler(Looper.getMainLooper()).post {
                if (isSuccessful) {
                    Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    Toast.makeText(context, "Erro ao cadastrar: $errorBody", Toast.LENGTH_LONG).show()
                }
            }
        }
    })
}
