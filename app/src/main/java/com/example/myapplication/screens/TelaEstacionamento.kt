package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEstacionamentoAtivo(
    navController: NavController,
    horarioEntradaString: String?
) {
    // Defina sua regra de negócio. Ex: R$ 5,00 por hora.
    val tarifaPorHora = 5.0

    // Validação robusta da entrada
    val horarioEntrada = remember {
        try {
            if (horarioEntradaString != null) LocalDateTime.parse(horarioEntradaString, DateTimeFormatter.ISO_DATE_TIME) else null
        } catch (e: DateTimeParseException) {
            null // Retorna nulo se a string não estiver no formato esperado
        }
    }

    if (horarioEntrada == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erro: Horário de entrada inválido ou não encontrado.")
        }
        return
    }

    var duracao by remember { mutableStateOf(Duration.ZERO) }
    var custo by remember { mutableStateOf(0.0) }

    // Este `LaunchedEffect` funciona como um cronômetro, atualizando a UI a cada segundo.
    LaunchedEffect(key1 = Unit) {
        while (true) {
            // Calcula a duração entre o horário de entrada e o horário atual
            duracao = Duration.between(horarioEntrada, LocalDateTime.now())

            // Lógica de cálculo de custo (fração de hora conta como hora cheia)
            // Garante que a primeira hora seja cobrada, mesmo que por poucos segundos.
            val horasTotais = ceil(duracao.toSeconds() / 3600.0).coerceAtLeast(1.0)
            custo = horasTotais * tarifaPorHora

            delay(1000) // Pausa por 1 segundo
        }
    }

    // Formata a duração para o formato HH:MM:SS
    val tempoFormatado = remember(duracao) {
        val horas = duracao.toHours()
        val minutos = duracao.toMinutes() % 60
        val segundos = duracao.seconds % 60
        String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }

    // Formata o custo para o formato monetário R$ 0,00
    val custoFormatado = remember(custo) {
        String.format("R$ %.2f", custo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessão Ativa") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tempo Decorrido",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = tempoFormatado,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Custo Atual",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = custoFormatado,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f)) // Empurra o botão para baixo

            Button(
                // ========================================================
                // ATUALIZAÇÃO PRINCIPAL AQUI
                // ========================================================
                onClick = {
                    // Navega para a câmera, especificando que o objetivo é o checkout.
                    navController.navigate("camera?scanMode=checkout")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Escanear QR Code de Saída"
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("PAGAR E FINALIZAR SESSÃO", fontSize = 16.sp)
            }
        }
    }
}
