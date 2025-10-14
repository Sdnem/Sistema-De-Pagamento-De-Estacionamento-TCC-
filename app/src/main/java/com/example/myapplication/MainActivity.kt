package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.model.DadosSessao
import com.example.myapplication.model.ResumoPagamentoData
import com.google.gson.Gson
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {
    // 1. Obtenha a instância do seu ViewModel.
    // O ViewModel será compartilhado entre todas as telas do fluxo de pagamento.
    val estacionamentoViewModel: EstacionamentoViewModel = viewModel()
    val pagamentoViewModel: PagamentoViewModel = viewModel()

    // 2. Observe o estado da UI para reagir e navegar automaticamente.
    val pagamentoUiState by pagamentoViewModel.uiState.collectAsState()

    LaunchedEffect(pagamentoUiState) {
        when (val state = pagamentoUiState) {
            is PagamentoUiState.Sucesso -> {
                // Navega para a tela de sucesso, passando o valor como argumento.
                // A rota "resumo" é removida da pilha para que o usuário não possa voltar para ela.
                navController.navigate("confirmado/${state.valorConfirmado}") {
                    popUpTo("resumo") { inclusive = true }
                }
            }
            is PagamentoUiState.Falha -> {
                // Navega para a tela de falha.
                navController.navigate("falha")
            }
            else -> { /* Não faz nada nos outros estados */ }
        }
    }

    // PADRONIZANDO OS NOMES DAS ROTAS PARA SEREM SIMPLES
    NavHost(navController = navController, startDestination = "login") {

        composable(route = "login") {
            TelaLogin(navController = navController)
        }
        composable(route = "home") {
            HomeScreen(navController = navController)
        }
        composable(route = "cadastro_usuario") {
            TelaCadastro(navController = navController)
        }
        composable(route = "cadastro_cartao") {
            CadastroCartaoScreen(navController = navController)
        }
        composable(route = "camera") { // Rota para a câmera
            CameraScreen(navController = navController)
        }
        composable(route = "cartoes") { // Rota para a lista de cartões
            TelaCartoes(navController = navController)
        }

        // --- ROTAS DO FLUXO DE PAGAMENTO ---

        // SEU CÓDIGO INTEGRADO AQUI
        composable(
            route = "resumo/{resumoDataJson}",
            arguments = listOf(navArgument("resumoDataJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val resumoDataJson = backStackEntry.arguments?.getString("resumoDataJson")
            val resumoData = resumoDataJson?.let { Gson().fromJson(it, ResumoPagamentoData::class.java) }
            val uiState by pagamentoViewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            LaunchedEffect(resumoData) {
                resumoData?.let { pagamentoViewModel.iniciarFluxoDePagamento(it) }
            }

            LaunchedEffect(uiState) {
                when (val currentState = uiState) {
                    is PagamentoUiState.Sucesso -> {
                        navController.navigate("confirmado/${currentState.valorConfirmado}") {
                            popUpTo("resumo/{resumoDataJson}") { inclusive = true }
                        }
                    }
                    is PagamentoUiState.Falha -> {
                        navController.navigate("falha") {
                            popUpTo("resumo/{resumoDataJson}") { inclusive = true }
                        }
                    }
                    else -> { /* Não faz nada */ }
                }
            }

            // 5. Exibir a UI correta com base no estado atual
            when (val currentState = uiState) {
                is PagamentoUiState.ProntoParaConfirmar -> {
                    TelaResumoPagamento(
                        resumoData = currentState.resumo,
                        // 2. Passe o context como parâmetro na chamada da função
                        onConfirmarPagamento = {
                            pagamentoViewModel.processarPagamento(context)
                        },
                        onVoltar = {
                            navController.popBackStack()
                        }
                    )
                }
                is PagamentoUiState.Carregando -> {
                    // Mostra a tela de resumo com um indicador de carregamento sobreposto
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (resumoData != null) {
                            TelaResumoPagamento(resumoData = resumoData, onConfirmarPagamento = {}, onVoltar = {})
                        }
                        CircularProgressIndicator()
                    }
                }
                // Os estados de Sucesso e Falha não precisam mais de UI aqui,
                // pois a navegação já foi disparada.
                is PagamentoUiState.Sucesso, is PagamentoUiState.Falha, null -> {
                    // Tela de carregamento inicial enquanto os dados não chegam
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator()
                    }
                }

                PagamentoUiState.Ocioso -> TODO()
            }
        }

        composable(
            route = "confirmado/{valor}",
            arguments = listOf(navArgument("valor") { type = NavType.StringType })
        ) { backStackEntry ->
            val valor = backStackEntry.arguments?.getString("valor") ?: "R$ 0,00"

            TelaDePagamentoConfirmado(
                amountPaid = valor,
                onConcludeClick = {
                    // Após concluir, vai para a home e limpa a pilha de navegação do pagamento.
                    navController.navigate("home") {
                        popUpTo("resumo") { inclusive = true }
                    }
                }
            )
        }

        composable(route = "falha") {
            // A tela de falha também recebe apenas ações.
            TelaDeFalhaNoPagamento(
                onRetryClick = {
                    // A ação de tentar novamente reseta o estado no ViewModel e volta.
                    pagamentoViewModel.tentarNovamente()
                    navController.popBackStack()
                },
                onBackToHomeClick = {
                    // Volta para a home e limpa a pilha de navegação do pagamento.
                    navController.navigate("home") {
                        popUpTo("resumo") { inclusive = true }
                    }
                }
            )
        }

        // Rota para a tela de estacionamento ativo
        composable(
            route = "estacionamento_ativo/{horario_entrada}",
            arguments = listOf(navArgument("horario_entrada") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val horarioEntrada = backStackEntry.arguments?.getString("horario_entrada")
            TelaEstacionamentoAtivo(
                navController = navController,
                horarioEntradaString = horarioEntrada,
                estacionamentoViewModel = estacionamentoViewModel
            )
        }
    }
}

