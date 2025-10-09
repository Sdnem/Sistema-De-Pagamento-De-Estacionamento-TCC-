package com.example.myapplication

import TelaCartoes
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.screens.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Tema padrão do app
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Chama a tela inicial
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "cadastro"
    ) {
        composable("login") { TelaLogin(navController) }
        composable("cadastro") { TelaCadastro(navController) }
        composable("home") { TelaPrincipal(navController) }
        composable("cartoes") { TelaCartoes(navController) }
        composable("carros") { TelaCarros( viewModel(), navController) }
        composable("cadastro_cartao") { TelaCadastroCartao( viewModel(), navController) }
    }
}

@Composable
fun TelaPrincipal(navController: NavHostController) {

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    App()
}
