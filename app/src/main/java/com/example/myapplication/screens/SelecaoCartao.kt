package com.example.myapplication.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.SelecaoCartaoUiState
import com.example.myapplication.SelecaoCartaoViewModel
import com.example.myapplication.remote.CartaoResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSelecaoCartao(
    // Supondo que você use uma factory para injetar o ViewModel
    viewModel: SelecaoCartaoViewModel,
    // Ação para ser executada quando o usuário confirmar a seleção
    onProximoClicked: (cartaoSelecionado: CartaoResponse) -> Unit,
    onVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var cartaoSelecionado by remember { mutableStateOf<CartaoResponse?>(null) }

    // Carrega os cartões quando a tela é iniciada
    LaunchedEffect(Unit) {
        // OBS: O ID do usuário deve ser obtido de forma dinâmica (ex: de um gerenciador de sessão)
        viewModel.carregarCartoes(usuarioId = 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolha o Cartão") },
                // Adicione um botão de voltar se necessário
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    cartaoSelecionado?.let { onProximoClicked(it) }
                },
                enabled = cartaoSelecionado != null, // Habilita o botão apenas se um cartão for selecionado
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("CONTINUAR PARA O RESUMO")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is SelecaoCartaoUiState.Carregando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SelecaoCartaoUiState.Falha -> {
                    Text(
                        text = state.mensagem,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is SelecaoCartaoUiState.Sucesso -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(state.cartoes) { cartao ->
                            ItemCartao(
                                cartao = cartao,
                                isSelected = cartao.id == cartaoSelecionado?.id,
                                onSelected = { cartaoSelecionado = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCartao(
    cartao: CartaoResponse,
    isSelected: Boolean,
    onSelected: (CartaoResponse) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(cartao) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pode adicionar um ícone de cartão de crédito aqui
        Column(modifier = Modifier.weight(1f)) {
            Text(text = cartao.nome, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Final •••• ${cartao.numero.takeLast(4)}", style = MaterialTheme.typography.bodyMedium)
        }
        RadioButton(
            selected = isSelected,
            onClick = { onSelected(cartao) }
        )
    }
    Divider()
}