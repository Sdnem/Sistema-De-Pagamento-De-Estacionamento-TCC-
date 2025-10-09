package com.example.myapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.model.Cartao
import com.example.myapplication.teste.CartaoViewModelTeste
import com.example.myapplication.teste.FakeCartaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastroCartao(
    viewModel: CartaoViewModelTeste = viewModel(),
    navController: NavHostController
) {
    // Estados para os campos de texto. Usar String é a prática padrão.
    var banco by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var validade by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }

    // ✅ NOVO E MELHORADO: Efeito que escuta o fluxo de eventos
    LaunchedEffect(key1 = true) { // key1 = true faz com que ele rode apenas uma vez
        viewModel.cadastroEvent.collect {
            // Este bloco será executado toda vez que um evento for emitido
            navController.navigate("cartoes") {
                popUpTo("cartoes") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cadastro de Cartão", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMPOS DE TEXTO ---
        TextField(
            value = banco,
            onValueChange = { banco = it },
            label = { Text("Banco") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome do Titular") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = numero,
            onValueChange = { if (it.length <= 16) numero = it.filter { char -> char.isDigit() } },
            label = { Text("Número do Cartão") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = validade,
            onValueChange = { if (it.length <= 4) validade = it.filter { char -> char.isDigit() } },
            label = { Text("Validade (MMAA)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = cvc,
            onValueChange = { if (it.length <= 3) cvc = it.filter { char -> char.isDigit() } },
            label = { Text("CVC") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Mostra o indicador de progresso se estiver carregando
        if (viewModel.isLoading.value) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val novoCartao = Cartao(
                        banco = banco,
                        nome = nome,
                        numero = numero.toLongOrNull() ?: 0L, // Conversão segura
                        validade = validade.toIntOrNull() ?: 0,
                        cvc = cvc.toIntOrNull() ?: 0, // Conversão segura
                        userId = 1 // ❗ Lembre-se de substituir pela lógica real para obter o ID do usuário
                    )
                    // A UI apenas notifica a ViewModel sobre a intenção do usuário
                    viewModel.addCartao(novoCartao)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastrar Cartão")
            }
        }

        // Mostra a mensagem de erro, se houver
        viewModel.errorMessage.value?.let { errorMsg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMsg, color = Color.Red)
        }
    }
}

// Preview do estado padrão (formulário vazio)
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Cadastro - Estado Padrão")
@Composable
fun TelaCadastroCartaoPreview_Default() {
    MaterialTheme {
        TelaCadastroCartao(
            viewModel = FakeCartaoViewModel(initialIsLoading = false, initialErrorMessage = null),
            navController = rememberNavController()
        )
    }
}

// Preview do estado de "Carregando"
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Cadastro - Carregando")
@Composable
fun TelaCadastroCartaoPreview_Loading() {
    MaterialTheme {
        TelaCadastroCartao(
            viewModel = FakeCartaoViewModel(initialIsLoading = true), // <-- A mágica acontece aqui
            navController = rememberNavController()
        )
    }
}

// Preview com uma mensagem de erro
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Cadastro - Com Erro")
@Composable
fun TelaCadastroCartaoPreview_Error() {
    MaterialTheme {
        TelaCadastroCartao(
            viewModel = FakeCartaoViewModel(initialErrorMessage = "Número de cartão inválido."), // <-- E aqui
            navController = rememberNavController()
        )
    }
}