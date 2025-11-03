package com.example.myapplication.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.model.SessionManager // <-- Garanta que está importado
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

@Composable
fun CadastroCartaoScreen(navController: NavController) {
    var numeroCartao by remember { mutableStateOf("") }
    var nomeTitular by remember { mutableStateOf("") }
    var dataValidade by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cadastre um Cartão",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "É necessário ter um método de pagamento para continuar.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // ... (Todos os seus OutlinedTextFields continuam aqui, sem alteração) ...
            OutlinedTextField(
                value = numeroCartao,
                onValueChange = {
                    // Limita o número do cartão a 16 caracteres
                    if (it.length <= 16) {
                        numeroCartao = it
                    }
                },
                label = { Text("Número do Cartão") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nomeTitular,
                onValueChange = { nomeTitular = it },
                label = { Text("Nome do Titular") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dataValidade,
                    onValueChange = { newValue ->
                        // Lógica para formatar a data como MM/AA

                        // Filtra para manter apenas dígitos
                        val digits = newValue.filter { it.isDigit() }
                        // Limita a 4 dígitos (MMYY)
                        val truncatedDigits = digits.take(4)

                        val formattedDate = when {
                            // Se tiver 2 ou menos dígitos, apenas os exibe (ex: "01")
                            truncatedDigits.length <= 2 -> truncatedDigits
                            // Se tiver 3 ou 4 dígitos, adiciona a barra (ex: "01/2" ou "01/25")
                            else -> "${truncatedDigits.substring(0, 2)}/${truncatedDigits.substring(2)}"
                        }

                        dataValidade = formattedDate
                    },
                    label = { Text("Validade (MM/AA)") },
                    // O input total terá 5 caracteres (MM/AA)
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = cvv,
                    onValueChange = {
                        // Limita o tamanho do CVV a 3 dígitos
                        if (it.length <= 3){
                            cvv = it
                        }
                    },
                    label = { Text("CVV") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {
                    if (isLoading) return@Button
                    isLoading = true

                    scope.launch {
                        // ================================================
                        // CORREÇÃO PRINCIPAL AQUI
                        // ================================================

                        // 1. Pega o token salvo na sessão
                        val token = SessionManager.getAuthToken(context)
                        if (token == null) {
                            Toast.makeText(context, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show()
                            navController.navigate("login") { popUpTo(0) } // Volta tudo
                            isLoading = false
                            return@launch
                        }

                        // 2. Prepara os dados do cartão
                        val cartaoJson = JsonObject().apply {
                            addProperty("numero", numeroCartao)
                            addProperty("nome", nomeTitular)
                            addProperty("validade", dataValidade)
                            addProperty("cvv", cvv)
                        }

                        // 3. Faz a chamada na API, incluindo o token no Header
                        try {
                            // Adicionamos "Bearer $token" como primeiro argumento
                            val response = RetrofitClient.api.cadastrarCartao("Bearer $token", cartaoJson)

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Cartão cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home") {
                                    // Limpa a navegação para o usuário não voltar para esta tela
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            } else {
                                // O erro "Credenciais inválidas" cairá aqui
                                val errorMsg = response.errorBody()?.string() ?: "Erro ao cadastrar cartão."
                                Log.e("CADASTRO_CARTAO_ERRO", "Código: ${response.code()} | Mensagem: $errorMsg")
                                Toast.makeText(context, "Erro: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("CADASTRO_CARTAO_EXCECAO", "Exceção: ${e.message}")
                            Toast.makeText(context, "Falha na conexão com o servidor.", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                        // ================================================
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Salvar Cartão e Continuar")
                }
            }
        }
    }
}
