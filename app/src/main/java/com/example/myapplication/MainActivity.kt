package com.example.myapplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme

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
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    val estacionamentoViewModel: EstacionamentoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        // Rotas existentes
        composable(route = "login") { TelaLogin(navController = navController) }
        composable(route = "home") { HomeScreen(navController = navController, estacionamentoViewModel = estacionamentoViewModel) }
        composable(route = "cadastro_usuario") { TelaCadastro(navController = navController) }
        composable(route = "cadastro_cartao") { CadastroCartaoScreen(navController = navController) }
        composable(route = "cartoes") { TelaCartoes(navController = navController) }

        // Rota para o fluxo de CHECK-IN
        composable(
            route = "exibir_qrcode_entrada/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            ExibirQrCodeEntradaScreen(
                navController = navController,
                token = token
            )
        }

        // Rota para o fluxo de CHECKOUT
        composable(
            route = "exibir_qrcode_saida/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            ExibirQrCodeSaidaScreen(
                navController = navController,
                token = token
            )
        }

        // Rota para a tela de sessão de estacionamento ativa
        composable(
            route = "estacionamento_ativo/{horario_entrada}",
            arguments = listOf(navArgument("horario_entrada") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val horarioEntrada = backStackEntry.arguments?.getString("horario_entrada")
            TelaEstacionamento(
                navController = navController,
                horarioEntradaString = horarioEntrada,
                estacionamentoViewModel = estacionamentoViewModel
            )
        }

        // ========================================================
        //              NOVA ROTA ADICIONADA AQUI
        // ========================================================
        /**
         * Rota para a tela de confirmação de pagamento.
         * Ela recebe o valor pago como um argumento opcional do tipo Float.
         */
        composable(
            route = "confirmacao_pagamento?valor={valorPago}",
            arguments = listOf(
                navArgument("valorPago") {
                    type = NavType.FloatType
                    defaultValue = 0.0f // Define um valor padrão para segurança
                }
            )
        ) { backStackEntry ->
            // Extrai o argumento da rota para passá-lo para a tela
            val valor = backStackEntry.arguments?.getFloat("valorPago")
            TelaConfirmacao(navController = navController, valorPago = valor)
        }
    }
}
