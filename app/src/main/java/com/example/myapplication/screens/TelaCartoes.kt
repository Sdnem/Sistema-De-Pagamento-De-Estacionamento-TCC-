package com.example.myapplication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.CartaoViewModel
import com.example.myapplication.ListaCartoesState
import com.example.myapplication.OperationState
import com.example.myapplication.remote.CartaoResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCartoes(
    navController: NavController,
    cartaoViewModel: CartaoViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // --- CORREÇÃO APLICADA AQUI ---
    // Este `DisposableEffect` substitui o `LaunchedEffect(key1 = true)`.
    // Ele observa o ciclo de vida da tela e busca os cartões sempre que a tela
    // se torna visível (evento ON_RESUME).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cartaoViewModel.buscarCartoes(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // --- FIM DA CORREÇÃO ---

    val listaState by cartaoViewModel.listaCartoesState.collectAsState()
    val definirPadraoState by cartaoViewModel.definirPadraoState.collectAsState()
    val excluirState by cartaoViewModel.excluirCartaoState.collectAsState()

    // Observador para o estado de "definir padrão"
    LaunchedEffect(definirPadraoState) {
        when (val state = definirPadraoState) {
            is OperationState.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.data) }
                cartaoViewModel.resetDefinirPadraoState()
            }
            is OperationState.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message, withDismissAction = true) }
                cartaoViewModel.resetDefinirPadraoState()
            }
            else -> {}
        }
    }

    // Observador para o estado de "excluir"
    LaunchedEffect(excluirState) {
        when (val state = excluirState) {
            is OperationState.Success -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.data) }
                cartaoViewModel.resetExcluirCartaoState()
            }
            is OperationState.Error -> {
                coroutineScope.launch { snackbarHostState.showSnackbar(state.message, withDismissAction = true) }
                cartaoViewModel.resetExcluirCartaoState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
        val isLoading = listaState is ListaCartoesState.Loading ||
                definirPadraoState is OperationState.Loading ||
                excluirState is OperationState.Loading

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = listaState) {
                is ListaCartoesState.Loading -> {
                    // O indicador de progresso global já lida com o estado de loading inicial.
                }
                is ListaCartoesState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { cartaoViewModel.buscarCartoes(context) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
                is ListaCartoesState.Success -> {
                    if (state.cartoes.isEmpty()) {
                        TelaVaziaCartoes()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.cartoes, key = { it.id }) { cartao ->
                                CartaoItem(
                                    cartao = cartao,
                                    onDefinirPadrao = {
                                        cartaoViewModel.definirComoPadrao(context, cartao.id)
                                    },
                                    onExcluir = {
                                        cartaoViewModel.excluirCartao(context, cartao.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Overlay de carregamento global
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(color = Color.Black.copy(alpha = 0.3f), modifier = Modifier.fillMaxSize()){}
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun CartaoItem(
    cartao: CartaoResponse,
    onDefinirPadrao: () -> Unit,
    onExcluir: () -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }
    var mostrarDialogoExclusao by remember { mutableStateOf(false) }

    val borda = if (cartao.is_default) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

    if (mostrarDialogoExclusao) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoExclusao = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza de que deseja excluir o cartão final ${cartao.numero?.takeLast(4)}? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExcluir()
                        mostrarDialogoExclusao = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoExclusao = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = borda
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CreditCard,
                contentDescription = "Ícone de Cartão",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Final ${cartao.numero?.takeLast(4) ?: "????"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!cartao.bandeira.isNullOrBlank()) {
                    Text(
                        text = cartao.bandeira.replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (cartao.is_default) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Cartão Padrão",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Menu de Opções
            Box {
                IconButton(onClick = { menuAberto = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                }
                DropdownMenu(
                    expanded = menuAberto,
                    onDismissRequest = { menuAberto = false }
                ) {
                    // Ação: Definir como Padrão (só aparece se não for o padrão)
                    if (!cartao.is_default) {
                        DropdownMenuItem(
                            text = { Text("Definir como padrão") },
                            onClick = {
                                onDefinirPadrao()
                                menuAberto = false
                            },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                        )
                    }
                    // Ação: Excluir
                    DropdownMenuItem(
                        text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            mostrarDialogoExclusao = true
                            menuAberto = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}

@Composable
fun TelaVaziaCartoes() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CreditCardOff,
            contentDescription = "Nenhum cartão",
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum cartão cadastrado",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Adicione um novo cartão de crédito para começar a usar o estacionamento.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}
