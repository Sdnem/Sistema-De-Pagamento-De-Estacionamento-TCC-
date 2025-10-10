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
import com.google.gson.JsonObject
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
                            val loginRequest = JsonObject().apply {
                                addProperty("email", email)
                                addProperty("senha", senha)
                            }

                            val response = RetrofitClient.api.login(loginRequest)

                            if (response.isSuccessful && response.body() != null) {
                                val responseBody = response.body()!!
                                Log.d("LOGIN_SUCCESS", "Login bem-sucedido: $responseBody")

                                // ========================================================
                                // CORREÇÃO: Extrair o ID e também o NOME do usuário
                                // ========================================================
                                val usuarioJson = responseBody.getAsJsonObject("usuario")
                                val userId = usuarioJson.get("id").asInt
                                // Usamos 'get' para pegar o nome e convertemos para String.
                                // 'asString' funciona, mas get()?.asString é mais seguro se o nome puder ser nulo.
                                val userName = usuarioJson.get("nome")?.asString

                                // CORREÇÃO: Usar a nova função 'saveUserData' que salva ambos
                                SessionManager.saveUserData(context, userId, userName)

                                Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home") {
                                    // Limpa a pilha de navegação para que o usuário não volte para o login
                                    popUpTo("login") { inclusive = true }
                                }

                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Credenciais inválidas"
                                Log.e("LOGIN_FAIL", "Erro: $errorMsg")
                                Toast.makeText(context, "Credenciais inválidas", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("API_LOGIN", "Exceção: ${e.message}")
                            Toast.makeText(context, "Falha na conexão com o servidor", Toast.LENGTH_LONG).show()
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
            // CORREÇÃO: A rota para cadastro é 'cadastro_usuario', conforme MainActivity.kt
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
