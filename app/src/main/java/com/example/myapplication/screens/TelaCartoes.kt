package com.example.myapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
// CORREÇÃO: Removido o ponto extra na linha de importação
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.CartaoViewModel
import com.example.myapplication.ListaCartoesState
import com.example.myapplication.remote.CartaoResponse

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TelaCartoes(
    navController: NavController,
    cartaoViewModel: CartaoViewModel = viewModel() // Injeta o ViewModel
) {
    val context = LocalContext.current

    // `LaunchedEffect` é executado uma vez quando a tela é carregada.
    // Ele dispara a busca pelos cartões no ViewModel.
    LaunchedEffect(key1 = true) {
        cartaoViewModel.buscarCartoes(context)
    }

    // Observa o estado da lista de cartões do ViewModel. A tela será recomposta sempre que o estado mudar.
    val listaState by cartaoViewModel.listaCartoesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Cartões") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("cadastro_cartao") }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Cartão")
            }
        }
    ) { paddingValues ->

        // O `when` decide o que mostrar na tela com base no estado atual
        when (val state = listaState) {
            is ListaCartoesState.Loading -> {
                // Mostra um indicador de carregamento no centro
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ListaCartoesState.Error -> {
                // Mostra a mensagem de erro no centro
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Erro ao carregar cartões.\n${state.message}", textAlign = TextAlign.Center)
                }
            }
            is ListaCartoesState.Success -> {
                if (state.cartoes.isEmpty()) {
                    // Se a lista de sucesso estiver vazia, mostra uma mensagem amigável
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Você ainda não possui cartões cadastrados.", textAlign = TextAlign.Center)
                    }
                } else {
                    // Se a lista tiver itens, usa uma LazyColumn para exibi-los
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.cartoes) { cartao ->
                            CartaoItem(cartao = cartao)
                        }
                    }
                }
            }
        }
    }
}

// Composable reutilizável para exibir um único cartão na lista
@Composable
fun CartaoItem(cartao: CartaoResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                // Garante que a chamada está correta
                imageVector = Icons.Filled.CreditCard,
                contentDescription = "Ícone de Cartão",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cartao.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = cartao.numero, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Validade: ${cartao.validade}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
