package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.ResumoPagamentoData

// 2. A tela principal (Composable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaResumoPagamento(
    resumoData: ResumoPagamentoData,
    onConfirmarPagamento: () -> Unit,
    onVoltar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Pagamento") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            // Botão de confirmação fixo na parte inferior
            Button(
                onClick = onConfirmarPagamento,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Text("Pagar ${resumoData.valorTotal}", fontSize = 18.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção 1: Detalhes do Veículo
            CartaoDetalhes(titulo = "Seu Veículo") {
                InfoLinha(label = "Placa", valor = resumoData.placaVeiculo)
                InfoLinha(label = "Modelo", valor = resumoData.modeloVeiculo)
            }

            // Seção 2: Detalhes do Estacionamento
            CartaoDetalhes(titulo = "Período Utilizado") {
                InfoLinha(label = "Entrada", valor = resumoData.entrada)
                InfoLinha(label = "Saída", valor = resumoData.saida)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                InfoLinha(label = "Tempo Total", valor = resumoData.tempoTotal, isValorDestaque = true)
            }

            // Seção 3: Detalhes do Pagamento
            CartaoDetalhes(titulo = "Resumo Financeiro") {
                InfoLinha(label = "Método", valor = "") // Deixado em branco para o ícone
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = "Ícone Cartão",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${resumoData.metodoPagamento} final ${resumoData.finalCartao}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                InfoLinha(label = "Valor a Pagar", valor = resumoData.valorTotal, isValorDestaque = true)
            }
        }
    }
}

// 3. Componentes auxiliares para reutilização de código

@Composable
fun CartaoDetalhes(
    titulo: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun InfoLinha(label: String, valor: String, isValorDestaque: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = if (isValorDestaque) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isValorDestaque) FontWeight.Bold else FontWeight.Normal,
            color = if (isValorDestaque) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// 4. Preview para visualizar no Android Studio
@Preview(showBackground = true)
@Composable
fun TelaResumoPagamentoPreview() {
    val dadosExemplo = ResumoPagamentoData(
        placaVeiculo = "BRA2E19",
        modeloVeiculo = "Honda Civic",
        entrada = "07/10/2025 14:30",
        saida = "07/10/2025 16:45",
        tempoTotal = "2h 15min",
        valorTotal = "R$ 12,00",
        metodoPagamento = "Crédito",
        finalCartao = "1234"
    )
    // Para o preview, o MaterialTheme é necessário
    MaterialTheme {
        TelaResumoPagamento(
            resumoData = dadosExemplo,
            onConfirmarPagamento = {}, // Ação vazia para o preview
            onVoltar = {} // Ação vazia para o preview
        )
    }
}