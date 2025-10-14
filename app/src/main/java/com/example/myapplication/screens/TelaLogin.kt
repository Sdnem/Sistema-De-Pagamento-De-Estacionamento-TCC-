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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun TelaLogin(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bem-vindo!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Faça login para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isLoading) return@Button

                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.login(email = email, senha = senha)

                            if (response.isSuccessful && response.body() != null) {
                                val loginResponse = response.body()!!

                                // Salvar os dados da sessão IMEDIATAMENTE
                                SessionManager.saveAuthToken(context, loginResponse.access_token)
                                SessionManager.saveUserData(context, loginResponse.user_id, loginResponse.user_name)

                                Log.d("LOGIN_SUCCESS", "UserID: ${loginResponse.user_id}, Sessão Ativa: ${loginResponse.active_session_info}")
                                Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                                // ========================================================
                                // LÓGICA DE REDIRECIONAMENTO INTELIGENTE
                                // ========================================================
                                // Prioridade 1: Usuário já tem uma sessão de estacionamento ativa?
                                if (loginResponse.active_session_info != null) {
                                    navController.navigate("estacionamento_ativo/${loginResponse.active_session_info.horario_entrada}") {
                                        // Limpa toda a pilha de navegação para que o usuário não volte
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                                // Prioridade 2: Usuário não tem cartão cadastrado?
                                else if (loginResponse.card_count == 0) {
                                    navController.navigate("cadastro_cartao") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                // Prioridade 3: Tudo certo, vai para a tela inicial.
                                else {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                // ========================================================

                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Credenciais inválidas"
                                Log.e("LOGIN_FAIL", "Erro: $errorMsg")
                                Toast.makeText(context, "Credenciais inválidas", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("API_LOGIN", "Exceção: ${e.message}")
                            Toast.makeText(context, "Falha na conexão com o servidor.", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Entrar", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("cadastro_usuario") }) {
                Text("Não tem uma conta? Cadastre-se")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaLoginPreview() {
    TelaLogin(navController = rememberNavController())
}
