package com.example.myapplication.screens

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
import com.example.myapplication.CarManagementViewModel
import com.example.myapplication.model.Carro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastroCarro(
    viewModel: CarManagementViewModel = viewModel(),
    navController: NavHostController
) {
    // Estados para os campos de texto. Usar String é a prática padrão.
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var cor by remember { mutableStateOf("") }

    // ✅ NOVO E MELHORADO: Efeito que escuta o fluxo de eventos
    LaunchedEffect(key1 = true) { // key1 = true faz com que ele rode apenas uma vez
        viewModel.cadastroEvent.collect {
            // Este bloco será executado toda vez que um evento for emitido
            navController.navigate("carros") {
                popUpTo("carros") { inclusive = true }
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
            value = marca,
            onValueChange = { marca = it },
            label = { Text("Marca") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = modelo,
            onValueChange = { modelo = it },
            label = { Text("Modelo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = placa,
            onValueChange = { if (it.length <= 7) placa = it.filter { char -> char.isDigit() } },
            label = { Text("Placa") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = ano,
            onValueChange = { if (it.length <= 4) ano = it.filter { char -> char.isDigit() } },
            label = { Text("Ano do Carro") },
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
                    val novoCarro = Carro(
                        marca = marca,
                        modelo = modelo,
                        ano = ano.toIntOrNull() ?: 0,
                        placa = placa,
                        cor = cor,
                        imageUrl = "URL de exemplo",
                        userId = 1 // ❗ Lembre-se de substituir pela lógica real para obter o ID do usuário,
                    )
                    // A UI apenas notifica a ViewModel sobre a intenção do usuário
                    viewModel.addCarro(novoCarro)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastrar Carro")
            }
        }

        // Mostra a mensagem de erro, se houver
        viewModel.errorMessage.value?.let { errorMsg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMsg, color = Color.Red)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaCadastroCarroPreview() {
    // Para o preview funcionar, você precisa de uma ViewModel de mock ou uma factory.
    // Por simplicidade, deixamos como está, mas a lógica agora está desacoplada.
    TelaCadastroCarro(navController = rememberNavController())
}