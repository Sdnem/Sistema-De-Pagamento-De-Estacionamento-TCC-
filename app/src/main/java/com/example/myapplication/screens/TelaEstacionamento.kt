package com.example.myapplication.screens

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.EstacionamentoViewModel
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEstacionamento(
    navController: NavController,
    horarioEntradaString: String?,
    // O ViewModel não é mais necessário aqui para o cálculo, mas mantemos se for usado para outra coisa.
    estacionamentoViewModel: EstacionamentoViewModel
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // Validação do horário de entrada (sem alterações)
    if (horarioEntradaString.isNullOrBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Erro: Horário de entrada não encontrado.") }
        return
    }
    val horarioEntrada = remember(horarioEntradaString) {
        try { LocalDateTime.parse(horarioEntradaString, DateTimeFormatter.ISO_DATE_TIME) } catch (e: DateTimeParseException) { null }
    }
    if (horarioEntrada == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Erro: Formato de horário inválido.") }
        return
    }

    // ========================================================
    //              MUDANÇA PRINCIPAL NA LÓGICA
    // ========================================================
    var tempoFormatado by remember { mutableStateOf("00:00:00") }
    // O custo agora começa com um placeholder até a API responder
    var custoFormatado by remember { mutableStateOf("R$ --,--") }
    val coroutineScope = rememberCoroutineScope()

    // Este LaunchedEffect agora tem uma dupla responsabilidade:
    // 1. Atualiza o cronômetro do tempo a cada segundo.
    // 2. Chama a API para buscar o preço real a cada 10 segundos.
    LaunchedEffect(key1 = horarioEntrada) {
        var a = 0
        while (true) {
            // Atualiza o tempo decorrido (lógica puramente visual)
            val agora = LocalDateTime.now()
            val duracao = Duration.between(horarioEntrada, agora)
            val horas = duracao.toHours()
            val minutos = duracao.toMinutes() % 60
            val segundos = duracao.seconds % 60
            tempoFormatado = String.format("%02d:%02d:%02d", horas, minutos, segundos)

            // A cada 10 segundos (e na primeira execução), busca o preço do backend
            if (a % 10 == 0) {
                coroutineScope.launch {
                    val valor = buscarPrecoDoBackend(context)
                    if (valor != null) {
                        // Atualiza o texto do custo com o valor vindo do servidor
                        custoFormatado = String.format("R$ %.2f", valor)
                    }
                }
            }
            a++
            delay(1000)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sua Sessão Ativa") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                InfoCard(
                    icon = Icons.Default.Timer,
                    label = "Tempo Decorrido",
                    value = tempoFormatado,
                    valueFontSize = 56.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                InfoCard(
                    icon = Icons.Default.Payments,
                    // O label agora indica a fonte do valor
                    label = "Custo (do Servidor)",
                    value = custoFormatado,
                    valueFontSize = 48.sp,
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    isLoading = true
                    realizarCheckout(context, navController) { isLoading = false }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 3.dp)
                } else {
                    Text("PAGAR E SAIR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ========================================================
//              NOVA FUNÇÃO PARA CHAMAR A API
// ========================================================
/**
 * Função que busca o valor de checkout previsto diretamente do backend.
 * É uma suspend function para ser chamada de dentro de uma corrotina.
 */
private suspend fun buscarPrecoDoBackend(context: Context): Float? {
    val token = SessionManager.getAuthToken(context)
    if (token == null) {
        Log.e("TelaEstacionamento", "Token nulo, não foi possível buscar o preço.")
        return null
    }

    return try {
        // Chama o novo endpoint definido no ApiService
        val response = RetrofitClient.api.preverValorCheckout("Bearer $token")
        if (response.isSuccessful && response.body() != null) {
            // Extrai o valor do JsonObject retornado pela API
            response.body()!!.get("valor_previsto").asFloat
        } else {
            Log.e("TelaEstacionamento", "Erro ao buscar preço: ${response.errorBody()?.string()}")
            null
        }
    } catch (e: Exception) {
        Log.e("TelaEstacionamento", "Exceção ao buscar preço: ${e.message}")
        null
    }
}

// O InfoCard reutilizável permanece o mesmo (sem alterações)
@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    valueFontSize: androidx.compose.ui.unit.TextUnit,
    valueColor: Color = LocalContentColor.current
) {
    // ... (código existente sem alterações)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
        }
    }
}


// A função de checkout permanece a mesma (sem alterações)
private fun realizarCheckout(
    context: Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    // ... (código existente sem alterações)
    val token = SessionManager.getAuthToken(context)
    if (token != null) {
        navController.navigate("exibir_qrcode_saida/$token")
    } else {
        Toast.makeText(context, "Sessão inválida. Por favor, faça login novamente.", Toast.LENGTH_LONG).show()
    }
    onComplete()
}
