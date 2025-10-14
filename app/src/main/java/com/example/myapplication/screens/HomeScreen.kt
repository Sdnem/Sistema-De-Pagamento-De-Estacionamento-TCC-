package com.example.myapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.EstacionamentoViewModel
import com.example.myapplication.model.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navController: NavController,
    estacionamentoViewModel: EstacionamentoViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observa o estado de check-in ativo
    val isCheckInActive by estacionamentoViewModel.isCheckInActive.collectAsState()

    // Verifica o status inicial uma única vez
    LaunchedEffect(Unit) {
        estacionamentoViewModel.verificarStatusCheckIn(context)
    }

    // CORREÇÃO FINAL: Especificando o tipo <String?> diretamente no mutableStateOf.
    // Esta é a sintaxe mais explícita e segura.
    val userName = remember { mutableStateOf<String?>(SessionManager.getUserName(context)) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Coleta o estado do ID da sessão ativa.
    // Use collectAsStateWithLifecycle para uma coleta segura em relação ao ciclo de vida.
    val sessaoId by estacionamentoViewModel.sessaoIdAtiva.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    label = { Text("Meus Cartões") },
                    selected = false,
                    onClick = {
                        navController.navigate("cartoes")
                        scope.launch { drawerState.close() }
                    }
                )
                // Adicione outros itens de menu aqui se necessário
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Bem-vindo(a)") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                            }
                        }
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        // Acessamos o valor com .value
                        text = "Olá, ${userName.value ?: "Usuário"}!",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                    Button(
                        onClick = {
                            navController.navigate("camera")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Ler QR Code",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("LER QR CODE DA ENTRADA", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            navController.navigate("cadastro_cartao")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = "Cadastrar Cartão",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("CADASTRAR NOVO CARTÃO", style = MaterialTheme.typography.titleMedium)
                    }

                    Button(
                        onClick = {
                            navController.navigate("resumo/{resumoDataJson}")
                            // 1. Garante que o ID não é nulo antes de usá-lo.
                            // O botão já estará desabilitado se for nulo, mas essa é uma segurança extra.
                            val id = sessaoId
                            if (id != null) {
                                // 2. Passa o ID para a função do ViewModel.
                                estacionamentoViewModel.finalizarSessaoEPreprarPagamento(id)
                            }
                        },
                        // Habilita o botão APENAS se houver uma sessão ativa COM um ID válido.
                        enabled = isCheckInActive && sessaoId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Finalizar e Pagar")
                    }
                }
            }
        }
    )
}

