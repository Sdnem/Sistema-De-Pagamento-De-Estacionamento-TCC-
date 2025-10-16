package com.example.myapplication.screens

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.CameraView
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.RetrofitClient
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    navController: NavController,
    scanMode: String // "checkin" ou "checkout"
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    LaunchedEffect(key1 = Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // A visualização da câmera fica no fundo
        CameraView(
            onBarCodeDetected = { barcodes ->
                if (isLoading) return@CameraView
                isLoading = true

                barcodes.firstOrNull()?.rawValue?.let { barCodeValue ->
                    handleScan(barCodeValue, scanMode, context, navController) {
                        isLoading = false
                    }
                }
            }
        )

        // Botão de simulação
        if (!isLoading) {
            FloatingActionButton(
                onClick = {
                    isLoading = true
                    val simulatedQrValue = if (scanMode == "checkout") "checkout" else "checkin"
                    handleScan(simulatedQrValue, scanMode, context, navController) {
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Simular Leitura de QR Code",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Indicador de progresso
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                strokeWidth = 4.dp
            )
        }
    }
}

// Função de tratamento unificada
private fun handleScan(
    barCodeValue: String,
    scanMode: String,
    context: Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    when (scanMode) {
        "checkin" -> handleCheckIn(barCodeValue, context, navController, onComplete)
        "checkout" -> handleCheckOut(barCodeValue, context, navController, onComplete)
        else -> {
            Toast.makeText(context, "Modo de scanner inválido.", Toast.LENGTH_LONG).show()
            navController.popBackStack()
            onComplete()
        }
    }
}

private fun handleCheckIn(
    barCodeValue: String,
    context: Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    if (!barCodeValue.equals("checkin", ignoreCase = true)) {
        showInvalidQRCodeAndGoBack(context, navController)
        onComplete()
        return
    }

    Toast.makeText(context, "QR Code de Check-in detectado!", Toast.LENGTH_SHORT).show()

    CoroutineScope(Dispatchers.IO).launch {
        val token = SessionManager.getAuthToken(context)
        if (token == null) {
            onComplete()
            return@launch
        }
        try {
            // ========================================================
            //              CORREÇÃO APLICADA AQUI
            // ========================================================
            // Cria o corpo JSON que a API agora espera.
            val checkInJson = com.google.gson.JsonObject().apply {
                addProperty("cancela_id", barCodeValue)
            }

            // Passa o token e o corpo JSON para a chamada da API.
            val response = RetrofitClient.api.registrarCheckIn("Bearer $token", checkInJson)
            // ========================================================

            CoroutineScope(Dispatchers.Main).launch {
                if (response.isSuccessful && response.body() != null) {
                    val checkInResponse = response.body()!!
                    Toast.makeText(context, "Check-in realizado!", Toast.LENGTH_LONG).show()
                    navController.navigate("estacionamento_ativo/${checkInResponse.horario_entrada}") {
                        popUpTo("home") { inclusive = true }
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erro no check-in"
                    Toast.makeText(context, "Erro: $errorMsg", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
            }
        } catch (e: Exception) {
            Log.e("CHECKIN_API", "Exceção ao fazer check-in: ${e.message}")
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Falha na conexão.", Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        } finally {
            CoroutineScope(Dispatchers.Main).launch { onComplete() }
        }
    }
}


private fun handleCheckOut(
    barCodeValue: String,
    context: Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    if (!barCodeValue.equals("checkout", ignoreCase = true)) {
        showInvalidQRCodeAndGoBack(context, navController)
        onComplete()
        return
    }

    Toast.makeText(context, "Confirmando pagamento e finalizando sessão...", Toast.LENGTH_SHORT).show()

    CoroutineScope(Dispatchers.IO).launch {
        val token = SessionManager.getAuthToken(context)
        if (token == null) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Sessão expirada. Faça login.", Toast.LENGTH_LONG).show()
                navController.navigate("login") { popUpTo(0) }
            }
            onComplete()
            return@launch
        }

        try {
            val response = RetrofitClient.api.registrarCheckout("Bearer $token")

            CoroutineScope(Dispatchers.Main).launch {
                if (response.isSuccessful && response.body() != null) {
                    val checkoutResponse = response.body()!!
                    Toast.makeText(context, "Pagamento de R$ ${String.format("%.2f", checkoutResponse.valor_pago)} efetuado com sucesso!", Toast.LENGTH_LONG).show()
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erro ao fazer checkout"
                    Toast.makeText(context, "Erro: $errorMsg", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
            }
        } catch (e: Exception) {
            Log.e("CHECKOUT_API", "Exceção: ${e.message}")
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Falha na conexão com o servidor.", Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        } finally {
            CoroutineScope(Dispatchers.Main).launch { onComplete() }
        }
    }
}


private fun showInvalidQRCodeAndGoBack(context: Context, navController: NavController) {
    Toast.makeText(context, "QR Code inválido.", Toast.LENGTH_LONG).show()
    navController.popBackStack()
}
