package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.CadastroViewModel

@Composable
fun TelaCadastro(
    navController: NavHostController,
    cadastroViewModel: CadastroViewModel = viewModel() // Injeta o ViewModel
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val cadastroSucesso by rememberUpdatedState(cadastroViewModel.cadastroSucesso)

    // Efeito para navegar quando o cadastro for bem-sucedido
    LaunchedEffect(cadastroSucesso) {
        if (cadastroSucesso) {
            // Navega para a tela de login para que o usuário possa entrar
            navController.navigate("login") {
                // Limpa a pilha para que o usuário não volte para a tela de cadastro
                popUpTo("welcome") { inclusive = true }
            }
            // Reseta o estado no ViewModel após a navegação
            cadastroViewModel.onNavegacaoCompleta()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Criar Nova Conta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // A única responsabilidade do botão é chamar o ViewModel
                cadastroViewModel.cadastrarUsuario(nome, email, senha)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cadastrar")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaCadastroPreview() {
    TelaCadastro(navController = rememberNavController())
}
