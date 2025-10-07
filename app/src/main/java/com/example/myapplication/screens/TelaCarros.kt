package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.CarManagementViewModel
import com.example.myapplication.model.Carro
import com.example.myapplication.model.SessionManager
import com.example.myapplication.teste.FakeSessionManager

// 1. O Composable principal da Tela
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCarros(
    viewModel: CarManagementViewModel = viewModel(),
    //navController: NavHostController
) {
    val carList by viewModel.carros.collectAsState()
    val canAddCar = carList.size < 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Carros") },
                actions = {
                    Button(
                        onClick = { viewModel.addExampleCar() },
                        // O botão é desabilitado se a lista estiver cheia
                        enabled = canAddCar
                    ) {
                        Text("+ Adicionar Carro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        // Lógica para mostrar o estado correto: vazio ou com conteúdo
        if (carList.isEmpty()) {
            EmptyState(
                onAddCarClick = { viewModel.addExampleCar() },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            CarList(
                cars = carList,
                onEditClick = { /* TODO: Lógica de edição */ },
                onDeleteClick = { carId -> viewModel.deletarCarro(carId) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

// 2. Composable para a lista de carros
@Composable
fun CarList(
    cars: List<Carro>,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Usamos Column com scroll pois serão no máximo 3 itens.
    // LazyColumn seria um exagero aqui.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Espaço entre os cards
    ) {
        cars.forEach { carro ->
            CarCard(
                carro = carro,
                onEditClick = {
                    // A função onEditClick SÓ será chamada se carro.id não for nulo.
                    // Dentro deste bloco, 'id' é um Int (não-nulo).
                    carro.id?.let { id ->
                        onEditClick(id)
                    }
                },
                onDeleteClick = {
                    // A mesma lógica segura para o clique de exclusão.
                    carro.id?.let { id ->
                        onDeleteClick(id)
                    }
                }
            )
        }
    }
}

// 3. Composable para o Card de um Carro
@Composable
fun CarCard(
    carro: Carro,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = carro.imageUrl,
                contentDescription = "Foto do ${carro.modelo}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${carro.marca} ${carro.modelo}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Carro")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Carro", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Placa: ${carro.placa}", style = MaterialTheme.typography.bodyMedium)
                Text("Ano: ${carro.ano}", style = MaterialTheme.typography.bodyMedium)
                Text("Cor: ${carro.cor}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// 4. Composable para o Estado Vazio
@Composable
fun EmptyState(
    onAddCarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = "Ícone de carro",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Você ainda não adicionou nenhum carro.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddCarClick) {
            Text("Adicionar meu primeiro carro")
        }
    }
}

// Preview para visualizar no Android Studio
@Preview(showBackground = true)
@Composable
fun TelaCarrosPreview_WithCars() {
    val fakeSessionManager = FakeSessionManager()
    val previeViewModel = CarManagementViewModel(fakeSessionManager)
    previeViewModel.addExampleCar() // Adiciona o HB20 de exemplo

    TelaCarros(viewModel = previeViewModel)
}

@Preview(showBackground = true)
@Composable
fun TelaCarrosPreview_Empty() {
    MaterialTheme {
        val fakeSessionManager = FakeSessionManager()
        val carroViewModel = CarManagementViewModel(sessionManager = fakeSessionManager)
        val previewViewModel = carroViewModel

        // Forçando o estado vazio
        previewViewModel.deletarCarro(1) // Remove o carro inicial
        TelaCarros(viewModel = previewViewModel)
    }
}