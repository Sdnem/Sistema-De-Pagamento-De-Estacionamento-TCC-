package com.example.myapplication.screens

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
import com.example.myapplication.CartaoViewModel
import com.example.myapplication.CartaoViewModelFactory
import com.example.myapplication.model.SessionManager

@Composable
fun TelaCartoes(
    navController: NavController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isLoggedIn = remember { sessionManager.fetchAuthToken() != null }

    // CORREÇÃO: Adicionada verificação de login antes de carregar a tela
    if (!isLoggedIn) {
        // LaunchedEffect garante que a navegação ocorra de forma segura no ciclo de vida do Composable.
        // A chave `true` garante que isso execute apenas uma vez.
        LaunchedEffect(true) {
            navController.navigate("login") {
                // Limpa todo o histórico de navegação para que o usuário não possa voltar
                // para uma tela interna pressionando o botão "Voltar".
                popUpTo(0)
            }
        }
        // Exibe um contêiner vazio enquanto redireciona para evitar que o resto do código execute
        return
    }

    // O código abaixo só será executado se o usuário estiver logado.
    // Isso evita o crash ao inicializar a ViewModel sem um token.
    val viewModel: CartaoViewModel = viewModel(
        factory = CartaoViewModelFactory(sessionManager)
    )

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
                items(cartoes, key = { it.id!! }) { cartao ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nome: ${cartao.nome}")
                            Text("Número: **** **** **** ${cartao.numero.toString().takeLast(4)}")
                            Text("Validade: ${cartao.dataValidade}")
                            Text("Banco: ${cartao.banco}")

                            Button(
                                onClick = {
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
                    navController.navigate("cadastro_cartao")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar Cartão")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaCartoesPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Preview da Tela de Cartões")
    }
}
