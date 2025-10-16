package com.example.myapplication.screens

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.remote.CheckOutResponse
import com.example.myapplication.remote.RetrofitClient
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExibirQrCodeSaidaScreen(navController: NavController, token: String?) {
    val context = LocalContext.current
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Validação do Token (sem alterações)
    if (token.isNullOrBlank()) {
        Text("Erro: Token de autenticação não encontrado.", color = MaterialTheme.colorScheme.error)
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Erro de autenticação.", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        return
    }

    LaunchedEffect(token) {
        qrCodeBitmap = generateQrCode(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagamento na Saída") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = !isLoading) { // Desabilita durante o loading
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        AnimatedContent(
            targetState = qrCodeBitmap != null,
            label = "QRCodeLoadingAnimation",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { isQrCodeReady ->
            if (isQrCodeReady) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Apresente na Saída", // Título mais direto
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // ========================================================
                        //              MENSAGEM MELHORADA AQUI
                        // ========================================================
                        Text(
                            text = "Para liberar a cancela e concluir o pagamento, aponte este código para o leitor.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(32.dp))

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code para saída",
                                modifier = Modifier
                                    .size(280.dp)
                                    .padding(20.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                simularCheckOut(context, token) { response ->
                                    if (response != null) {
                                        val valorPago = response.valor_pago
                                        navController.navigate("confirmacao_pagamento?valor=$valorPago") {
                                            // Limpa a pilha até a home, removendo a tela de estacionamento ativo
                                            popUpTo("home") { inclusive = false }
                                        }
                                    } else {
                                        // Se a simulação falhar, permite que o usuário tente novamente
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(100)),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text("SIMULAR LEITURA E PAGAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            } else {
                // Tela de carregamento (sem alterações)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Gerando código de saída...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// As funções auxiliares simularCheckOut e generateQrCode permanecem as mesmas
private suspend fun simularCheckOut(context: Context, token: String, onResult: (CheckOutResponse?) -> Unit) {
    Toast.makeText(context, "Processando pagamento...", Toast.LENGTH_SHORT).show() // Mensagem amigável
    try {
        val response = RetrofitClient.api.registrarCheckout("Bearer $token")
        if (response.isSuccessful && response.body() != null) {
            onResult(response.body())
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Erro na simulação de checkout"
            Toast.makeText(context, "Falha no pagamento: $errorMsg", Toast.LENGTH_LONG).show()
            onResult(null)
        }
    } catch (e: Exception) {
        Log.e("SIMULACAO_CHECKOUT", "Exceção: ${e.message}")
        Toast.makeText(context, "Falha na conexão. Verifique sua internet.", Toast.LENGTH_LONG).show()
        onResult(null)
    }
}

private fun generateQrCode(content: String): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}
