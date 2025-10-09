package com.example.myapplication.teste

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.model.Carro
import com.example.myapplication.screens.CarList
import com.example.myapplication.screens.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCarrosContent(
    carList: List<Carro>,
    onAddCarClick: () -> Unit,
    onDeleteCarClick: (Int) -> Unit,
    onEditCarClick: (Int) -> Unit
) {
    val canAddCar = carList.size < 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Carros") },
                actions = {
                    Button(
                        onClick = onAddCarClick,
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
        if (carList.isEmpty()) {
            EmptyState(
                onAddCarClick = onAddCarClick,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            CarList(
                cars = carList,
                onEditClick = onEditCarClick,
                onDeleteClick = onDeleteCarClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}