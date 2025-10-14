package com.example.myapplication.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.myapplication.EstacionamentoViewModel
import com.example.myapplication.model.DadosSessao
import com.google.gson.Gson
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEstacionamentoAtivo(
    navController: NavController,
    horarioEntradaString: String?,
    estacionamentoViewModel: EstacionamentoViewModel
) {
    // Defina sua regra de negócio. Ex: R$ 5,00 por hora.
    val tarifaPorHora = 5.0

    val sessaoId by estacionamentoViewModel.sessaoIdAtiva.collectAsStateWithLifecycle()
    val eventoNavegacao by estacionamentoViewModel.eventoDeNavegacaoResumo.collectAsStateWithLifecycle()

    if (horarioEntradaString == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Erro: Horário de entrada não encontrado.")
        }
        return
    }

    // Converte o horário (String) recebido da navegação para um objeto de data/hora
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val horarioEntrada = remember { LocalDateTime.parse(horarioEntradaString, formatter) }

    var duracao by remember { mutableStateOf(Duration.ZERO) }
    var custo by remember { mutableStateOf(0.0) }

    // Este `LaunchedEffect` é o coração da tela, funciona como um cronômetro.
    // Ele executa o bloco de código a cada 1 segundo.
    LaunchedEffect(key1 = Unit) {
        while (true) {
            // Calcula a duração entre o horário de entrada e o horário atual
            duracao = Duration.between(horarioEntrada, LocalDateTime.now())

            // Lógica de cálculo de custo (fração de hora conta como hora cheia)
            val horasTotais = ceil(duracao.toMillis() / 3600000.0).coerceAtLeast(1.0)
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

    // Efeito que observa o evento de navegação
    LaunchedEffect(eventoNavegacao) {
        eventoNavegacao?.let { resumoData ->
            // 1. Serializa o objeto para uma string JSON
            val resumoDataJson = Gson().toJson(resumoData)

            // IMPORTANTE: Codifica a string JSON para ser segura para URLs
            val rotaJsonCodificada = URLEncoder.encode(resumoDataJson, StandardCharsets.UTF_8.name())

            // 2. Navega para a tela de resumo com os dados
            navController.navigate("resumo/$rotaJsonCodificada")

            // 3. Informa ao ViewModel que o evento foi consumido
            estacionamentoViewModel.onNavegacaoParaResumoFeita()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sessão Ativa") })
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
                style = MaterialTheme.typography.titleLarge
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
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = custoFormatado,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = {
                    sessaoId?.let {
                        estacionamentoViewModel.finalizarSessaoEPreprarPagamento(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Finalizar e Pagar")
            }
        }
    }
}
