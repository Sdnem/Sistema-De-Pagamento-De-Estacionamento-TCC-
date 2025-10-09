import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.CartaoViewModel
import com.example.myapplication.CartaoViewModelFactory
import com.example.myapplication.model.Cartao
import com.example.myapplication.model.SessionManagerImpl

@Composable
fun TelaCartoes(
    navController: NavController,
    // A ViewModel é injetada aqui. O Context é necessário para o SessionManager.
    viewModel: CartaoViewModel = viewModel(
        factory = CartaoViewModelFactory(SessionManagerImpl(LocalContext.current))
    )
) {
    // Coleta o estado da ViewModel. A UI irá recompor automaticamente quando esses valores mudarem.
    val cartoes by viewModel.cartoes.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Meus Cartões", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Se houver uma mensagem de erro, exiba-a
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartoes, key = { it.id!! }) { cartao -> // Use o id como chave para melhor performance
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nome: ${cartao.nome}")
                            Text("Número: **** **** **** ${cartao.numero % 10000}")
                            Text("Banco: ${cartao.banco}")

                            Button(
                                onClick = {
                                    // A lógica de remoção agora está na ViewModel.
                                    // O ID do cartão pode ser nulo ao criar, mas nunca será ao ser listado do BD.
                                    cartao.id?.let { viewModel.deletarCartao(it) }
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
                    navController.navigate("addCartao")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar Cartão")
            }
        }
    }
}

@Preview(showBackground = true, name = "Tela de Cartões - Estado Vazio")
@Composable
fun TelaCartoesPreview_EstadoVazio() {
    MaterialTheme {
        // A chamada padrão para a tela já resulta no estado vazio,
        // pois o ViewModel da preview não tem acesso ao banco de dados.
        TelaCartoes(navController = rememberNavController())
    }
}

/**
 * Preview 2: Tela com uma lista de cartões já cadastrados.
 * Para isso, recriamos a UI da tela e fornecemos uma lista de dados falsos (mock data),
 * permitindo visualizar como os itens do LazyColumn serão renderizados.
 */
@Preview(showBackground = true, name = "Tela de Cartões - Com Lista")
@Composable
fun TelaCartoesPreview_ComListaDeCartoes() {
    // Lista de dados falsos para a preview
    val listaDeCartoesFalsos = listOf(
        Cartao(id = 1, nome = "J. SILVA", numero = 1111222233334444L, banco = "Banco Digital", validade = 2025, cvc = 123, userId = 1),
        Cartao(id = 2, nome = "M. PEREIRA", numero = 5555666677778888L, banco = "Banco Vermelho", validade = 2028, cvc = 321, userId = 2),
        Cartao(id = 3, nome = "A. COSTA", numero = 9999888877776666L, banco = "Caixa", validade = 2030, cvc = 987, userId = 3)
    )

    MaterialTheme {
        // Recriamos a estrutura da "TelaCartoes" aqui para poder injetar os dados falsos.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Meus Cartões", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Usamos a lista de dados falsos no LazyColumn
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(listaDeCartoesFalsos, key = { it.id!! }) { cartao ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nome: ${cartao.nome}")
                        Text("Número: **** **** **** ${cartao.numero % 10000}")
                        Text("Banco: ${cartao.banco}")

                        Button(
                            onClick = { /* Ação de remover na preview não faz nada */ },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // [cite: 65]
                        ) {
                            Text("Remover")
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Ação de adicionar na preview não faz nada */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar Cartão")
            }
        }
    }
}