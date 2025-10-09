package com.example.myapplication.teste

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastroCarroContent(
    // Parâmetros para os estados dos campos de texto
    marca: String,
    modelo: String,
    ano: String,
    placa: String,
    // Parâmetros para os eventos de mudança de valor
    onMarcaChange: (String) -> Unit,
    onModeloChange: (String) -> Unit,
    onAnoChange: (String) -> Unit,
    onPlacaChange: (String) -> Unit,
    // Parâmetros para o estado da UI
    isLoading: Boolean,
    errorMessage: String?,
    // Parâmetro para o evento de clique
    onCadastrarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cadastro de Carro", style = MaterialTheme.typography.headlineSmall) // Corrigi o título aqui
        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMPOS DE TEXTO ---
        TextField(
            value = marca,
            onValueChange = onMarcaChange, // Usa a função do parâmetro
            label = { Text("Marca") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = modelo,
            onValueChange = onModeloChange, // Usa a função do parâmetro
            label = { Text("Modelo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = placa,
            // A lógica de filtro pode ficar aqui ou na ViewModel
            onValueChange = { if (it.length <= 7) onPlacaChange(it) },
            label = { Text("Placa") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = ano,
            onValueChange = { if (it.length <= 4) onAnoChange(it.filter { char -> char.isDigit() }) },
            label = { Text("Ano do Carro") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Mostra o indicador de progresso se estiver carregando
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onCadastrarClick, // Usa a função do parâmetro
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastrar Carro")
            }
        }

        // Mostra a mensagem de erro, se houver
        errorMessage?.let { errorMsg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMsg, color = Color.Red)
        }
    }
}