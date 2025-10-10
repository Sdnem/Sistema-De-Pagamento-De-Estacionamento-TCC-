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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaDePagamentoConfirmado(
    amountPaid: String, // Parâmetro para receber o valor do pagamento
    onConcludeClick: () -> Unit // Função a ser chamada quando o botão for clicado
) {
    // Usamos um Box para preencher toda a tela e centralizar o conteúdo facilmente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            // Alinha todos os itens da coluna no centro horizontalmente
            horizontalAlignment = Alignment.CenterHorizontally,
            // Distribui o espaço verticalmente
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Ícone de Confirmação (Check)
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Ícone de Pagamento Confirmado",
                modifier = Modifier.size(100.dp),
                tint = Color(0xFF4CAF50) // Um tom de verde vibrante (Material Green 500)
            )

            // Espaçador para dar uma respiração entre o ícone e o texto
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Mensagem de Confirmação
            Text(
                text = "Pagamento Confirmado!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Espaçador opcional para um subtítulo, se desejado
            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo (opcional)
            Text(
                text = "Sua transação foi concluída com sucesso.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Espaçador maior para dar foco ao valor
            Spacer(modifier = Modifier.height(48.dp))

            // 3. Valor Pago
            Text(
                text = amountPaid,
                fontSize = 48.sp, // Fonte bem grande para destaque máximo
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Usamos outro Box para alinhar o botão na parte inferior da tela
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp), // Margem inferior para o botão
            contentAlignment = Alignment.BottomCenter
        ) {
            // Botão de Ação (Opcional)
            Button(
                onClick = onConcludeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Concluir",
                    fontSize = 18.sp
                )
            }
        }
    }
}

// A anotação @Preview permite visualizar o Composable no Android Studio
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaDePagamentoConfirmadoPreview() {
    // Para o preview, passamos um valor de exemplo e uma ação vazia
    // Você pode envolver com o seu tema do app para uma visualização mais fiel
    // YourAppTheme {
    TelaDePagamentoConfirmado(
        amountPaid = "R$ 12,00",
        onConcludeClick = {}
    )
    // }
}