package com.example.myapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.model.SessionManager
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // ModalNavigationDrawer é o componente que cria o menu lateral
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Conteúdo do Menu Lateral
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                Divider()
                NavigationDrawerItem(
                    label = { Text("Meus Cartões") },
                    selected = false,
                    onClick = {
                        navController.navigate("cartoes")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Meus Carros") },
                    selected = false,
                    onClick = {
                        navController.navigate("carros")
                        scope.launch { drawerState.close() }
                    }
                )
                Divider()
                NavigationDrawerItem(
                    label = { Text("Sair (Logout)") },
                    selected = false,
                    onClick = {
                        // Limpa a sessão e volta para a tela de login
                        sessionManager.clearAuthData()
                        navController.navigate("login") {
                            popUpTo(0) // Limpa todo o histórico de navegação
                        }
                    }
                )
            }
        }
    ) {
        // Conteúdo Principal da Tela
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tela Inicial") },
                    navigationIcon = {
                        // Ícone de menu que abre o drawer
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            // Corpo da tela inicial
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Aplica o padding do Scaffold
                    .padding(16.dp), // Adiciona nosso próprio padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Bem-vindo!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate("cartoes") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gerenciar Meus Cartões")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("carros") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gerenciar Meus Carros")
                }
            }
        }
    }
}
