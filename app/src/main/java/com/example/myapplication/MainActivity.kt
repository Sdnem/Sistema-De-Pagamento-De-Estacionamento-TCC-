package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Garanta que esta importação usa o pacote correto das suas telas
import com.example.myapplication.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme // Garanta que seu tema está aqui

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // É uma boa prática envolver seu App no seu tema customizado
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
        // CORREÇÃO APLICADA AQUI:
        startDestination = "cadastro" // A primeira tela agora é a de cadastro
    ) {
        composable("login") { TelaLogin(navController) }
        composable("cadastro") { TelaCadastro(navController) }
        composable("home") { HomeScreen(navController) }
        composable("cartoes") { TelaCartoes(navController) }
        composable("carros") { TelaCarros(navController) }
        composable("cadastro_cartao") { CadastroCartaoScreen(navController) }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    MyApplicationTheme {
        App()
    }
}
