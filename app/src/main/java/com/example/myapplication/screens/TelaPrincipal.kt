import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// O Composable principal que monta a tela inteira
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainParkingScreen() {
    // Estado para simular se há um ticket ativo ou não
    // No app real, isso viria de um ViewModel
    var hasActiveTicket by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Olá, [Nome do Usuário]!") },
                navigationIcon = {
                    IconButton(onClick = { /* Abre o menu lateral */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Abre as notificações */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificações")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        // Column principal com scroll para se adaptar a telas menores
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp) // Espaçamento entre os elementos
        ) {
            // Alterna a visualização baseada no estado 'hasActiveTicket'
            if (hasActiveTicket) {
                ActiveTicketInfo()
            } else {
                ScanNewTicketAction()
            }

            // Cards de acesso rápido
            InfoCard(
                icon = Icons.Default.DirectionsCar,
                title = "Veículo Cadastrado",
                line1 = "ABC-1234",
                line2 = "Honda Civic",
                actionText = "Trocar",
                onActionClick = { /* Lógica para trocar de veículo */ }
            )

            InfoCard(
                icon = Icons.Default.CreditCard,
                title = "Forma de Pagamento",
                line1 = "Crédito Final 5678",
                actionText = "Alterar",
                onActionClick = { /* Lógica para alterar o cartão */ }
            )

            RecentHistory()

            // Botão para simular o escaneamento e trocar o estado da tela
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { hasActiveTicket = !hasActiveTicket }) {
                Text(if (hasActiveTicket) "Simular Saída" else "Simular Leitura de Ticket")
            }
        }
    }
}

// Composable para a ação de escanear um novo ticket
@Composable
fun ScanNewTicketAction() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { /* Lógica para abrir a câmera */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("ESCANEAR NOVO TICKET", fontSize = 18.sp)
        }
        Text(
            text = "Aponte a câmera para o QR Code do seu ticket",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Composable para mostrar as informações de um ticket ativo
@Composable
fun ActiveTicketInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Shopping Center Plaza", style = MaterialTheme.typography.titleLarge)
            Text("Tempo Decorrido", style = MaterialTheme.typography.bodyMedium)
            Text("00:15:32", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Valor a Pagar", style = MaterialTheme.typography.bodyMedium)
            Text("R$ 7,50", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Text("Veículo: ABC-1234", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* Lógica para realizar o pagamento */ },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("REALIZAR PAGAMENTO", fontSize = 16.sp)
            }
        }
    }
}

// Card reutilizável para mostrar informações (veículo, cartão)
@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    line1: String,
    line2: String? = null,
    actionText: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(line1, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (line2 != null) {
                    Text(line2, style = MaterialTheme.typography.bodyMedium)
                }
            }
            TextButton(onClick = onActionClick) {
                Text(actionText)
            }
        }
    }
}

// Composable para a seção de Histórico Recente
@Composable
fun RecentHistory() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Histórico Recente", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { /* Navega para a tela de histórico */ }) {
                Text("Ver Todos")
            }
        }
        Divider()
        HistoryItem("Shopping Plaza", "R$ 12,50", "Hoje")
        HistoryItem("Estacionamento Centro", "R$ 8,00", "Ontem")
    }
}

@Composable
fun HistoryItem(place: String, value: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(place, fontWeight = FontWeight.SemiBold)
            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}


// --- Previews para visualizar no Android Studio ---

@Preview(showBackground = true, name = "Tela Principal - Sem Ticket")
@Composable
fun MainScreenPreview_NoTicket() {
    // Para o preview funcionar, envolva-o em um tema.
    // Substitua `YourAppTheme` pelo nome do tema do seu projeto.
    // YourAppTheme {
    MainParkingScreen()
    // }
}

@Preview(showBackground = true, name = "Tela Principal - Com Ticket Ativo")
@Composable
fun MainScreenPreview_WithTicket() {
    // Crio um Composable separado para forçar o estado no preview
    val ScreenWithTicketActive: @Composable () -> Unit = {
        var hasActiveTicket by remember { mutableStateOf(true) } // Força o estado inicial para true
        // O resto é igual ao original, mas não vou repetir a lógica inteira aqui.
        // A maneira mais fácil é modificar o MainParkingScreen para aceitar um estado inicial.
        // Por simplicidade aqui, você pode mudar o `mutableStateOf(false)` para `true` no Composable principal
        // para visualizar o outro estado no preview.
    }
    // YourAppTheme {
    // O ideal seria MainParkingScreen(initialState = true)
    // Mas para este exemplo, altere a linha 18 para `mutableStateOf(true)` para ver este preview.
    // }
}