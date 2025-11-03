package com.example.myapplication.screens

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.ActiveSessionInfo
import com.example.myapplication.remote.CheckInResponse
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExibirQrCodeEntradaScreen(navController: NavController, token: String?) {
    val context = LocalContext.current
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (token.isNullOrBlank()) {
        Text("Erro: Token de autenticação não encontrado.", color = MaterialTheme.colorScheme.error)
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Erro de autenticação. Tente novamente.", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        return
    }

    LaunchedEffect(token) {
        qrCodeBitmap = generateQrCode(token)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val sessionInfo = verificarStatusSessao(context)
            if (sessionInfo != null) {
                Toast.makeText(context, "Check-in confirmado!", Toast.LENGTH_SHORT).show()

                navController.navigate("estacionamento_ativo/${sessionInfo.horario_entrada}") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
                break
            }
            delay(3000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrada no Estacionamento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = !isLoading) {
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
                // Conteúdo principal
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
                            // ========================================================
                            //              1. TÍTULO PRINCIPAL SIMPLIFICADO
                            // ========================================================
                            text = "Apresente na Entrada",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Para liberar sua entrada, aponte este código para o leitor.",
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
                                contentDescription = "QR Code para entrada no estacionamento",
                                modifier = Modifier
                                    .size(280.dp)
                                    .padding(20.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            // ========================================================
                            //              2. TEXTO DE STATUS SIMPLIFICADO
                            // ========================================================
                            Text(
                                text = if (isLoading) "Processando..." else "Aguardando leitura...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Botão de simulação
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                val response = simularCheckIn(context, token)
                                if (response == null) {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(100)),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text("SIMULAR LEITURA DA ENTRADA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            } else {
                // Tela de carregamento
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Gerando seu código de acesso...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// As funções auxiliares (simularCheckIn, generateQrCode, verificarStatusSessao) não mudam
private suspend fun simularCheckIn(context: Context, token: String?): CheckInResponse? {
    if (token == null) return null
    Toast.makeText(context, "Conectando com a cancela...", Toast.LENGTH_SHORT).show()
    val checkInJson = JsonObject().apply { addProperty("cancela_id", "sim-01") }
    return try {
        val response = RetrofitClient.api.registrarCheckIn("Bearer $token", checkInJson)
        if (response.isSuccessful && response.body() != null) {
            // O Toast de sucesso aqui é para o desenvolvedor, o que é aceitável
            Toast.makeText(context, "Simulação bem-sucedida!", Toast.LENGTH_SHORT).show()
            response.body()
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Erro na simulação"
            Toast.makeText(context, "Falha na simulação: $errorMsg", Toast.LENGTH_LONG).show()
            null
        }
    } catch (e: Exception) {
        Log.e("SIMULACAO_CHECKIN", "Exceção: ${e.message}")
        Toast.makeText(context, "Falha na conexão durante a simulação.", Toast.LENGTH_LONG).show()
        null
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

private suspend fun verificarStatusSessao(context: Context): ActiveSessionInfo? {
    val token = SessionManager.getAuthToken(context) ?: return null
    return try {
        val response = RetrofitClient.api.verificarSessaoAtiva("Bearer $token")
        if (response.isSuccessful && response.body() != null) {
            response.body()
        } else {
            null
        }
    } catch (e: Exception) {
        Log.d("POLLING_STATUS", "Falha ao verificar status da sessão (normal durante a espera): ${e.message}")
        null
    }
}
