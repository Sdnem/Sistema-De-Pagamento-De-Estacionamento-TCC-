package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// CORREÇÃO: Importa o nome correto da tela (HomeScreen)
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
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

@Composable
fun NavGraph(navController: NavHostController) {
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
                horarioEntradaString = horarioEntrada
            )
        }
    }
}
