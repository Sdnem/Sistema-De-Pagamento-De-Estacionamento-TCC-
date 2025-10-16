package com.example.myapplication.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    var senhaVisivel by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Permite rolagem em telas menores
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- CABEÇALHO ---
            Text(
                text = "ParkEasy", // Um nome para o App dá um toque profissional
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Seu estacionamento, simplificado.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(40.dp))

            // --- CARTÃO DE LOGIN ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(all = 24.dp)) {
                    Text(
                        text = "Acesse sua conta",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // --- CAMPO DE EMAIL ---
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { // Ícone adicionado
                            Icon(Icons.Default.Email, contentDescription = "Ícone de Email")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- CAMPO DE SENHA ---
                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it },
                        label = { Text("Senha") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { // Ícone adicionado
                            Icon(Icons.Default.Lock, contentDescription = "Ícone de Senha")
                        },
                        trailingIcon = { // Ícone para mostrar/ocultar senha
                            val image = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(imageVector = image, description)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus() // Esconde o teclado ao pressionar Done
                        })
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // --- BOTÃO DE LOGIN ---
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            focusManager.clearFocus() // Esconde o teclado ao clicar no botão
                            isLoading = true
                            // A sua lógica de login permanece exatamente a mesma
                            scope.launch {
                                try {
                                    val response = RetrofitClient.api.login(email = email, senha = senha)
                                    if (response.isSuccessful && response.body() != null) {
                                        val loginResponse = response.body()!!
                                        SessionManager.saveAuthToken(context, loginResponse.access_token)
                                        SessionManager.saveUserData(context, loginResponse.user_id, loginResponse.user_name)
                                        Log.d("LOGIN_SUCCESS", "UserID: ${loginResponse.user_id}, Sessão Ativa: ${loginResponse.active_session_info}")
                                        Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                        if (loginResponse.active_session_info != null) {
                                            navController.navigate("estacionamento_ativo/${loginResponse.active_session_info.horario_entrada}") {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                            }
                                        } else if (loginResponse.card_count == 0) {
                                            navController.navigate("cadastro_cartao") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
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
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.medium // Bordas levemente arredondadas
                    ) {
                        // Caixa para centralizar o indicador de progresso
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text("Entrar", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTÃO DE CADASTRO ---
            TextButton(
                onClick = { if (!isLoading) navController.navigate("cadastro_usuario") },
                enabled = !isLoading
            ) {
                Text("Não tem uma conta? Cadastre-se")
            }
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun TelaLoginPreview() {
    // Para um preview mais fiel, você pode envolver com seu tema
    // import com.example.myapplication.ui.theme.MyApplicationTheme
    // MyApplicationTheme {
    TelaLogin(navController = rememberNavController())
    // }
}
