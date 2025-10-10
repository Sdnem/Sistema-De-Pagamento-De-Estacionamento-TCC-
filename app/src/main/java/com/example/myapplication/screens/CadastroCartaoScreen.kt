package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.CadastroCartaoState
import com.example.myapplication.CartaoViewModel

@Composable
fun CadastroCartaoScreen(navController: NavController) {
    // 1. Obtenha o ViewModel da forma correta (sem Factory)
    val cartaoViewModel: CartaoViewModel = viewModel()

    // 2. Observe o estado do ViewModel
    val state by cartaoViewModel.cadastroState.collectAsState()

    // Estados para os campos de texto
    var numeroCartao by remember { mutableStateOf("") }
    var nomeTitular by remember { mutableStateOf("") }
    var dataValidade by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val context = LocalContext.current

    // 3. Reaja às mudanças de estado (ex: mostrar Toasts ou navegar)
    LaunchedEffect(state) {
        when (val currentState = state) {
            is CadastroCartaoState.Success -> {
                Toast.makeText(context, "Cartão cadastrado com sucesso!", Toast.LENGTH_LONG).show()
                cartaoViewModel.resetState() // Reseta o estado para Idle
                navController.popBackStack() // Volta para a tela anterior
            }
            is CadastroCartaoState.Error -> {
                Toast.makeText(context, "Erro: ${currentState.message}", Toast.LENGTH_LONG).show()
                cartaoViewModel.resetState() // Reseta o estado para Idle
            }
            else -> { /* Não faz nada para Idle ou Loading */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Cadastro de Cartão", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = numeroCartao,
            onValueChange = { numeroCartao = it },
            label = { Text("Número do Cartão") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = nomeTitular,
            onValueChange = { nomeTitular = it },
            label = { Text("Nome do Titular") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Linha para Validade e CVV
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dataValidade,
                onValueChange = { dataValidade = it },
                label = { Text("Validade (MM/AAAA)") },
                modifier = Modifier.weight(1f) // Divide o espaço
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = cvv,
                onValueChange = { cvv = it },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f) // Divide o espaço
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Botão de salvar com estado de loading
        Button(
            onClick = {
                // 4. Chame a função do ViewModel, passando o contexto
                cartaoViewModel.cadastrarCartao(
                    context = context,
                    numero = numeroCartao,
                    nome = nomeTitular,
                    validade = dataValidade,
                    cvv = cvv
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            // Desabilita o botão enquanto está carregando
            enabled = state !is CadastroCartaoState.Loading
        ) {
            if (state is CadastroCartaoState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Salvar Cartão")
            }
        }
    }
}
