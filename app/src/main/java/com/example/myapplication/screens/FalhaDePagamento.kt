package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TelaDeFalhaNoPagamento(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ErrorOutline,
    title: String = "Pagamento Recusado",
    message: String = "Não foi possível processar seu pagamento. Verifique os dados e tente novamente.",
    specificError: String? = null, // Erro específico vindo da API, se houver
    onRetryClick: () -> Unit,
    onBackToHomeClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Ícone de Falha
        Icon(
            imageVector = icon,
            contentDescription = "Ícone de Falha no Pagamento",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Título Principal
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Mensagem Descritiva
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // 4. Exibe o erro específico, se existir
        specificError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Motivo: $it",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Botões de Ação
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botão Primário: Tentar Novamente
            Button(
                onClick = onRetryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "TENTAR NOVAMENTE")
            }

            // Botão Secundário: Voltar
            OutlinedButton(
                onClick = onBackToHomeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "VOLTAR PARA O INÍCIO")
            }
        }
    }
}

@Preview(showBackground = true, name = "Tela de Falha Padrão")
@Composable
fun TelaDeFalhaNoPagamentoPreview() {
    // Substitua AppTheme pelo seu tema
    TelaDeFalhaNoPagamento(
        onRetryClick = {},
        onBackToHomeClick = {}
    )
}

@Preview(showBackground = true, name = "Tela de Falha com Erro Específico")
@Composable
fun TelaDeFalhaNoPagamentoWithSpecificErrorPreview() {
    TelaDeFalhaNoPagamento(
        specificError = "Cartão de crédito expirado.",
        onRetryClick = {},
        onBackToHomeClick = {}
    )
}