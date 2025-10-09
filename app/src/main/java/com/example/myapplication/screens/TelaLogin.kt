package com.example.myapplication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.model.Usuario
import com.example.myapplication.remote.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.ui.Alignment

@Composable
fun TelaLogin(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally // Alinha o conteúdo no centro
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val usuario = Usuario(nome = "", email = email, senha = senha)

                RetrofitInstance.api.login(usuario).enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(
                        call: Call<Map<String, String>>,
                        response: Response<Map<String, String>>
                    ) {
                        if (response.isSuccessful && response.body()?.get("status") == "sucesso") {
                            navController.navigate("telaPrincipal")
                        } else {
                            mensagem = "Credenciais inválidas"
                        }
                    }

                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        Log.e("API", "Erro: ${t.message}")
                        mensagem = "Erro de conexão"
                    }
                })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        // --- BOTÃO DE RECUPERAÇÃO DE SENHA ADICIONADO AQUI ---
        TextButton(
            onClick = {
                // Navega para a nova tela de recuperação de senha
                navController.navigate("telaRecuperacaoSenha")
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Esqueceu a senha?")
        }
        // --------------------------------------------------------

        Spacer(modifier = Modifier.height(8.dp))
        // Mostra a mensagem de erro se ela não estiver vazia
        if (mensagem.isNotEmpty()) {
            Text(mensagem, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaLoginPreview() {
    TelaLogin(navController = rememberNavController())
}