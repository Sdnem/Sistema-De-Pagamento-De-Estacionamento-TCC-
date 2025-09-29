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
import com.example.myapplication.model.SessionManager
import com.example.myapplication.model.Cartao 

@Composable
fun TelaCartoes(
    navController: NavController,
    // A ViewModel é injetada aqui. O Context é necessário para o SessionManager.
    viewModel: CartaoViewModel = viewModel(
        factory = CartaoViewModelFactory(SessionManager(LocalContext.current))
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
                                    // O ID do cartão pode ser nulo ao criar, mas nunca será ao ser listado do BD. [cite: 5]
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

// Preview pode ser simplificado, pois a lógica está na ViewModel.
@Preview(showBackground = true)
@Composable
fun TelaCartoesPreview() {
    // Para o preview, podemos passar uma NavController de mentira.
    // A ViewModel não será criada no modo preview.
    // TelaCartoes(navController = rememberNavController())
    // O ideal seria criar um preview estático sem a viewModel
    Text("Preview da Tela de Cartões")
}