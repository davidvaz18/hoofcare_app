package com.example.hoof_care_02.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hoof_care_02.model.ChatbotMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyDv2O3cgnt3wOH2ux0yDAVLdsEqJdIxXF8"
        )
    }

    val chat = remember {
        generativeModel.startChat(
            history = listOf(
                content(role = "user") { text("A partir de agora, comporte-se como o Totó, um assistente amigável e prestativo para um aplicativo de chatbot. Seu mascote é um cachorro fofo e seu propósito é ajudar donos de cães com suas dúvidas. Não responda perguntas que não são relacionadas a cachorros. Responda de forma clara, positiva e evite respostas muito longas.") },
                content(role = "model") { text("Au! Estou pronto para ajudar os donos de cães. Qual é a primeira pergunta?") }
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatbotMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(ChatbotMessage("Au! Estou pronto para ajudar os donos de cães. Qual é a primeira pergunta?", "bot"))
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversar com o Totó") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Pergunte algo ao Totó...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val question = inputText.trim()
                            if (question.isNotEmpty()) {
                                messages.add(ChatbotMessage(question, "me"))
                                inputText = ""
                                isLoading = true

                                scope.launch {
                                    try {
                                        val response = chat.sendMessage(question)
                                        val responseText = response.text
                                        if (responseText != null) {
                                            messages.add(ChatbotMessage(responseText, "bot"))
                                        }
                                    } catch (e: Exception) {
                                        messages.add(ChatbotMessage("Desculpe, tive um problema. Tente novamente!", "bot"))
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Escreva algo", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isLoading,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF38C075))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatbotMessage) {
    val isMe = message.sentBy == "me"
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isMe) Color(0xFF38C075) else Color.White
    val textColor = if (isMe) Color.White else Color.Black
    val shape = if (isMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = shape,
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.message,
                color = textColor,
                modifier = Modifier.padding(12.dp),
                fontSize = 16.sp
            )
        }
    }
}
