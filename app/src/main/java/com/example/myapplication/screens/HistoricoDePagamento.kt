package com.example.myapplication.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.HistoricoDePagamento

// 2. A tela principal (Composable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHistoricoPagamentos(
    pagamentos: List<HistoricoDePagamento>,
    onVoltar: () -> Unit,
    onSelecionaPagamento: (String) -> Unit // Passa o ID do pagamento clicado
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Pagamentos") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Se a lista estiver vazia, mostra uma mensagem. Senão, mostra a lista.
        if (pagamentos.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum pagamento realizado ainda.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pagamentos) { pagamento ->
                    ItemHistoricoCard(
                        pagamento = pagamento,
                        onClick = { onSelecionaPagamento(pagamento.id) }
                    )
                }
            }
        }
    }
}

// 3. Composable para cada item da lista
@Composable
fun ItemHistoricoCard(
    pagamento: HistoricoDePagamento,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Torna o card clicável
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ícone e informações do veículo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Ícone de recibo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = pagamento.data,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Placa: ${pagamento.placaVeiculo}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Valor e Status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = pagamento.valor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = pagamento.status,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

// 4. Preview para visualizar no Android Studio
@Preview(showBackground = true, name = "Lista com Itens")
@Composable
fun TelaHistoricoPagamentosPreview() {
    val dadosExemplo = listOf(
        HistoricoDePagamento("1", "07 Out 2025, 16:45", "BRA2E19", "R$ 15,00", "Concluído"),
        HistoricoDePagamento("2", "28 Set 2025, 11:20", "ABC1D23", "R$ 8,50", "Concluído"),
        HistoricoDePagamento("3", "15 Set 2025, 18:00", "XYZ9H87", "R$ 20,00", "Concluído")
    )

    MaterialTheme {
        TelaHistoricoPagamentos(
            pagamentos = dadosExemplo,
            onVoltar = {},
            onSelecionaPagamento = {}
        )
    }
}

@Preview(showBackground = true, name = "Lista Vazia")
@Composable
fun TelaHistoricoPagamentosVaziaPreview() {
    MaterialTheme {
        TelaHistoricoPagamentos(
            pagamentos = emptyList(), // Passa uma lista vazia
            onVoltar = {},
            onSelecionaPagamento = {}
        )
    }
}