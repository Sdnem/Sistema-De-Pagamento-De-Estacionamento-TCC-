package com.example.myapplication.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.model.Cartao
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.RetrofitClient
import com.example.myapplication.ui.theme.MyApplicationTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroCartaoScreen(navController: NavController) {
    var numeroCartao by remember { mutableStateOf("") }
    var nomeTitular by remember { mutableStateOf("") }
    var dataValidade by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var banco by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Seus TextFields permanecem aqui, sem alterações ---
        TextField(
            value = numeroCartao,
            onValueChange = { novoTexto ->
                if (novoTexto.length <= 16 && novoTexto.all { it.isDigit() }) {
                    numeroCartao = novoTexto
                }
            },
            label = { Text("Número do Cartão (16 dígitos)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = nomeTitular,
            onValueChange = { nomeTitular = it },
            label = { Text("Nome do Titular") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = banco,
            onValueChange = { banco = it },
            label = { Text("Nome do Banco") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = dataValidade,
                onValueChange = { novoTexto ->
                    if (novoTexto.length <= 4 && novoTexto.all { it.isDigit() }) {
                        dataValidade = novoTexto
                    }
                },
                label = { Text("Validade (MMAA)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = cvc,
                onValueChange = { novoTexto ->
                    if (novoTexto.length <= 3 && novoTexto.all { it.isDigit() }) {
                        cvc = novoTexto
                    }
                },
                label = { Text("CVC") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val userId = sessionManager.fetchUserId()
                // CORREÇÃO: Obter o token para a chamada autenticada
                val token = sessionManager.fetchAuthToken()

                // Valida se o usuário está logado (possui ID e token)
                if (userId == null || token == null) {
                    Toast.makeText(context, "Erro: Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val cartao = Cartao(
                    id = null,
                    numero = numeroCartao.toLongOrNull() ?: 0L,
                    nome = nomeTitular,
                    dataValidade = if (dataValidade.length == 4) "${dataValidade.substring(0, 2)}/${dataValidade.substring(2)}" else dataValidade,
                    cvc = cvc.toIntOrNull() ?: 0,
                    banco = banco,
                    userId = userId
                )

                // CORREÇÃO APLICADA: Obter a instância do serviço passando o token
                val apiService = RetrofitClient.getInstance(token)
                val call = apiService.addCartao(cartao)

                call.enqueue(object : Callback<Cartao> {
                    override fun onResponse(call: Call<Cartao>, response: Response<Cartao>) {
                        if (response.isSuccessful) {
                            dialogMessage = "Cartão cadastrado com sucesso!"
                            showDialog = true
                        } else {
                            val errorBody = response.errorBody()?.string()
                            dialogMessage = "Erro ao cadastrar cartão: ${response.code()} - $errorBody"
                            showDialog = true
                            Log.e("CadastroCartao", "Erro: ${response.code()} - $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<Cartao>, t: Throwable) {
                        dialogMessage = "Falha na comunicação com o servidor: ${t.message}"
                        showDialog = true
                        Log.e("CadastroCartao", "Falha: ${t.message}")
                    }
                })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Cartão")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (dialogMessage.contains("sucesso")) "Sucesso" else "Erro") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    if (dialogMessage.contains("sucesso")) {
                        navController.popBackStack()
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCadastroCartaoScreen() {
    MyApplicationTheme {
        CadastroCartaoScreen(navController = rememberNavController())
    }
}
