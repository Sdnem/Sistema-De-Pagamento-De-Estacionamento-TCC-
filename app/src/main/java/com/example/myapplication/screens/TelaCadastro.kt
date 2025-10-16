// A PRIMEIRA LINHA DO ARQUIVO DEVE SER ESTA
package com.example.myapplication.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastro(navController: NavController) {
    var nome by remember { mutableStateOf("") }
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
                .verticalScroll(rememberScrollState()) // Permite rolagem
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- CABEÇALHO ---
            Text(
                text = "Crie sua Conta",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "É rápido e fácil.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- CARTÃO DE CADASTRO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // --- CAMPO DE NOME ---
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Ícone de Nome")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- CAMPO DE EMAIL ---
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
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
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Ícone de Senha")
                        },
                        trailingIcon = {
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
                            focusManager.clearFocus() // Fecha o teclado
                        })
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // --- BOTÃO DE CADASTRO ---
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            focusManager.clearFocus()
                            isLoading = true
                            // Sua lógica de cadastro permanece idêntica
                            scope.launch {
                                val usuarioJson = JsonObject().apply {
                                    addProperty("nome", nome)
                                    addProperty("email", email)
                                    addProperty("senha", senha)
                                }
                                try {
                                    val response = RetrofitClient.api.cadastrarUsuario(usuarioJson)
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("login") {
                                            popUpTo("cadastro_usuario") { inclusive = true }
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        val errorMsg = errorBody ?: "Erro no cadastro."
                                        Log.e("CADASTRO_USUARIO", errorMsg)
                                        Toast.makeText(context, "Erro: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("API_CADASTRO", "Exceção: ${e.message}")
                                    Toast.makeText(context, "Falha na conexão.", Toast.LENGTH_LONG).show()
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
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text("Criar Conta", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTÃO PARA VOLTAR AO LOGIN ---
            TextButton(
                onClick = { if (!isLoading) navController.popBackStack() },
                enabled = !isLoading
            ) {
                Text("Já tem uma conta? Faça login")
            }
        }
    }
}
