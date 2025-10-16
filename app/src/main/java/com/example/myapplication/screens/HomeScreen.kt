package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.EstacionamentoViewModel
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.HorariosPicoResponse
import com.example.myapplication.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- ViewModel e PicoState (sem alterações) ---
class HomeViewModel : ViewModel() {
    private val _horariosPicoState = MutableStateFlow<PicoState>(PicoState.Loading)
    val horariosPicoState = _horariosPicoState.asStateFlow()

    init {
        fetchHorariosPico()
    }

    fun fetchHorariosPico() {
        viewModelScope.launch {
            _horariosPicoState.value = PicoState.Loading
            try {
                val response = RetrofitClient.api.getHorariosPico()
                if (response.isSuccessful && response.body() != null) {
                    _horariosPicoState.value = PicoState.Success(response.body()!!)
                } else {
                    _horariosPicoState.value = PicoState.Error("Não foi possível carregar os dados.")
                }
            } catch (e: Exception) {
                _horariosPicoState.value = PicoState.Error("Falha na conexão.")
            }
        }
    }
}

sealed class PicoState {
    object Loading : PicoState()
    data class Success(val data: HorariosPicoResponse) : PicoState()
    data class Error(val message: String) : PicoState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(),
    estacionamentoViewModel: EstacionamentoViewModel = viewModel()
) {
    val context = LocalContext.current
    val picoState by homeViewModel.horariosPicoState.collectAsState()

    // Redirecionamento de sessão ativa (sem alterações)
    val activeSessionInfo by estacionamentoViewModel.activeSessionInfo.collectAsState()
    LaunchedEffect(activeSessionInfo) {
        activeSessionInfo?.let { horarioEntrada ->
            navController.navigate("estacionamento_ativo/$horarioEntrada") {
                popUpTo("home") { inclusive = true }
            }
            estacionamentoViewModel.onNavigationHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bem-vindo!") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Arrangement.SpaceAround para um espaçamento mais equilibrado
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Parte Superior: Boas-vindas
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Pronto para estacionar?",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Gere seu QR Code de entrada e verifique a lotação abaixo.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // ========================================================
            //              LAYOUT REORDENADO AQUI
            // ========================================================

            // 1. Botão de ação principal
            Button(
                onClick = {
                    val token = SessionManager.getAuthToken(context)
                    if (token != null) {
                        navController.navigate("exibir_qrcode_entrada/$token")
                    } else {
                        Toast.makeText(context, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Entrar",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Entrar no Estacionamento", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 2. Card de Movimentação
            HorarioPicoCard(
                state = picoState
            ) {
                homeViewModel.fetchHorariosPico()
            }


            // 3. Botão Secundário (agora no final da Column principal)
            OutlinedButton(
                onClick = { navController.navigate("cartoes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Meus Cartões")
            }
        }
    }
}


@Composable
fun HorarioPicoCard(
    state: PicoState,
    modifier: Modifier = Modifier, // Modifier como parâmetro
    onRetry: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = "Horários de Pico", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Movimentação Atual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is PicoState.Loading -> CircularProgressIndicator()
                    is PicoState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onRetry) {
                                Text("Tentar Novamente")
                            }
                        }
                    }
                    is PicoState.Success -> {
                        val lotacao = state.data.lotacao_percentual_atual
                        val corProgresso = when {
                            lotacao > 85 -> MaterialTheme.colorScheme.error
                            lotacao > 60 -> Color(0xFFFFA000) // Amber
                            else -> Color(0xFF388E3C) // Green
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = state.data.status_movimento_atual,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = corProgresso
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { lotacao / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = corProgresso,
                                trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
