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
// Importações das suas telas
import com.example.myapplication.screens.CadastroCartaoScreen
import com.example.myapplication.screens.CameraScreen
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.TelaCadastro
import com.example.myapplication.screens.TelaCartoes
import com.example.myapplication.screens.TelaEstacionamentoAtivo
import com.example.myapplication.screens.TelaLogin
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
        composable(route = "cartoes") {
            TelaCartoes(navController = navController)
        }

        // ========================================================
        // ATUALIZAÇÃO PRINCIPAL AQUI
        // ========================================================
        // A rota da câmera agora aceita um argumento para definir seu propósito.
        composable(
            route = "camera?scanMode={scanMode}", // O argumento é opcional na URL
            arguments = listOf(navArgument("scanMode") {
                type = NavType.StringType
                defaultValue = "checkin" // Se nada for passado, o padrão será "checkin"
            })
        ) { backStackEntry ->
            // Extrai o argumento 'scanMode' da rota
            val scanMode = backStackEntry.arguments?.getString("scanMode") ?: "checkin"

            // Passa o modo de escaneamento para a CameraScreen
            CameraScreen(navController = navController, scanMode = scanMode)
        }
        // ========================================================

        // Rota para a tela de estacionamento ativo (sem alterações aqui)
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
