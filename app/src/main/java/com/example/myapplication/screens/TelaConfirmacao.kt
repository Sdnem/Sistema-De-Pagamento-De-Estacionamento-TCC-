package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TelaConfirmacao(navController: NavController, valorPago: Float?) {
    // Formata o valor recebido para exibição, com um valor padrão caso seja nulo.
    val valorFormatado = "R$ ${String.format("%.2f", valorPago ?: 0.0f)}"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícone de sucesso
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Sucesso",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF4CAF50) // Um tom de verde para sucesso
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Título da confirmação
            Text(
                text = "Pagamento Realizado com Sucesso!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Exibição do valor pago
            Text(
                text = "Valor Pago",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valorFormatado,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Botão para voltar à tela inicial
            Button(
                onClick = {
                    navController.navigate("home") {
                        // Limpa toda a pilha de navegação para que o usuário
                        // não possa voltar para a tela de confirmação ou checkout.
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("VOLTAR À TELA INICIAL", fontSize = 16.sp)
            }
        }
    }
}
