import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

// Modelo do Cartão
data class Cartao(
    val nome: String,
    val numero: String,
    val banco: String
)

@Composable
fun CartoesScreen() {
    // Lista inicial de cartões (pode ser carregada do banco depois)
    var cartoes by remember {
        mutableStateOf(
            listOf(
                Cartao("Pedro Henrique", "**** **** **** 1234", "Banco do Brasil"),
                Cartao("Maria Silva", "**** **** **** 5678", "Itaú")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Meus Cartões",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(cartoes) { index, cartao ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Nome: ${cartao.nome}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Número: ${cartao.numero}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Banco: ${cartao.banco}", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                // Remove cartão
                                cartoes = cartoes.toMutableList().apply { removeAt(index) }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Remover")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Exemplo de adicionar cartão (depois você pode trocar por formulário)
                val novoCartao = Cartao("Novo Usuário", "**** **** **** ${(1000..9999).random()}", "Caixa")
                cartoes = cartoes + novoCartao
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar Cartão")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CartoesScreenPreview() {
    CartoesScreen()
}
