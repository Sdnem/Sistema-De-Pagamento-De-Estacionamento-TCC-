package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TelaRecuperacaoSenha() {
    // 1. Estados para armazenar os valores dos campos de texto
    var email by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Estrutura principal da tela
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda a tela
            .padding(horizontal = 16.dp), // Adiciona preenchimento nas laterais
        verticalArrangement = Arrangement.Center, // Centraliza os itens verticalmente
        horizontalAlignment = Alignment.CenterHorizontally // Centraliza os itens horizontalmente
    ) {
        // Título da tela
        Text(
            text = "Recuperação de Senha",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Informe seu e-mail e sua nova senha.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Campo de E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth() // Ocupa toda a largura
        )

        Spacer(modifier = Modifier.height(16.dp)) // Espaçamento entre os campos

        // Campo de Senha
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Nova Senha") },
            singleLine = true,
            // 2. Lógica para mostrar/esconder a senha
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            // 3. Ícone para alternar a visibilidade
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp)) // Espaçamento maior antes do botão

        // Botão para enviar
        Button(
            onClick = {
                // TODO: Adicionar a lógica de validação e chamada à API/Firebase aqui
                println("E-mail: $email")
                println("Nova Senha: $newPassword")
            },
            modifier = Modifier
                .fillMaxWidth() // Ocupa toda a largura
                .height(50.dp) // Define uma altura padrão
        ) {
            Text("REDEFINIR SENHA")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    TelaRecuperacaoSenha()
}