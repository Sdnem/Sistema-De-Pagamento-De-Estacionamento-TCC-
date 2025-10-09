package com.example.myapplication.screens

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.CarManagementViewModel
import com.example.myapplication.model.Carro
import com.example.myapplication.teste.TelaCadastroCarroContent
import androidx.compose.runtime.getValue

@Composable
fun TelaCadastroCarro(
    viewModel: CarManagementViewModel = viewModel(),
    navController: NavHostController
) {
    // Estados para os campos de texto, agora gerenciados aqui
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var cor by remember { mutableStateOf("") }

    // Observa o estado de carregamento e erro da ViewModel
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    // Efeito para navegar quando o cadastro for bem-sucedido
    LaunchedEffect(key1 = true) {
        viewModel.cadastroEvent.collect {
            navController.navigate("carros") {
                popUpTo("carros") { inclusive = true }
            }
        }
    }

    // Chama a Content, passando os estados e os eventos
    TelaCadastroCarroContent(
        marca = marca,
        modelo = modelo,
        ano = ano,
        placa = placa,
        onMarcaChange = { marca = it },
        onModeloChange = { modelo = it },
        onAnoChange = { ano = it },
        onPlacaChange = { placa = it },
        isLoading = isLoading,
        errorMessage = errorMessage,
        onCadastrarClick = {
            val novoCarro = Carro(
                marca = marca,
                modelo = modelo,
                ano = ano.toIntOrNull() ?: 0,
                placa = placa,
                cor = cor,
                imageUrl = "URL de exemplo",
                userId = 1
            )
            viewModel.addCarro(novoCarro)
        }
    )
}

@Preview(showBackground = true, name = "Estado Padrão")
@Composable
fun TelaCadastroCarroPreview() {
    TelaCadastroCarroContent(
        marca = "Ford",
        modelo = "Ka",
        ano = "2020",
        placa = "BRA2E19",
        onMarcaChange = {},
        onModeloChange = {},
        onAnoChange = {},
        onPlacaChange = {},
        isLoading = false,
        errorMessage = null,
        onCadastrarClick = {}
    )
}

@Preview(showBackground = true, name = "Estado de Carregamento")
@Composable
fun TelaCadastroCarroLoadingPreview() {
    TelaCadastroCarroContent(
        marca = "VW",
        modelo = "Nivus",
        ano = "2023",
        placa = "XYZ1234",
        onMarcaChange = {},
        onModeloChange = {},
        onAnoChange = {},
        onPlacaChange = {},
        isLoading = true, // <<-- Testando o loading
        errorMessage = null,
        onCadastrarClick = {}
    )
}

@Preview(showBackground = true, name = "Estado de Erro")
@Composable
fun TelaCadastroCarroErrorPreview() {
    TelaCadastroCarroContent(
        marca = "",
        modelo = "",
        ano = "",
        placa = "",
        onMarcaChange = {},
        onModeloChange = {},
        onAnoChange = {},
        onPlacaChange = {},
        isLoading = false,
        errorMessage = "Falha ao conectar. Tente novamente.", // <<-- Testando o erro
        onCadastrarClick = {}
    )
}