package com.example.myapplication.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.model.Cartao
import com.example.myapplication.remote.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun TelaCadastroCartao(navController: NavHostController) {
    var banco by remember { mutableStateOf("")}
    var nome by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf(0)}
    var validade by remember { mutableStateOf(0)}
    var cvc by remember { mutableStateOf(0)}

    var campo1 by remember { mutableStateOf(numero.toString())}
    var campo2 by remember { mutableStateOf(validade.toString())}
    var campo3 by remember { mutableStateOf(cvc.toString())}

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = banco,
            onValueChange = { banco = it },
            label = { Text("Banco") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = campo1,
            onValueChange = { novoTexto ->
                val textoFiltrado = novoTexto.filter { it.isDigit() }
                if (textoFiltrado.length <= 16) {
                    campo1 = textoFiltrado
                    numero = textoFiltrado.toIntOrNull() ?: 0
                }
            },
            label = { Text("Número do Cartão") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = campo2,
            onValueChange = { novoTexto ->
                val textoFiltrado = novoTexto.filter { it.isDigit() }
                if (textoFiltrado.length <= 4) {
                    campo2 = textoFiltrado
                    validade = textoFiltrado.toIntOrNull() ?: 0
                }
            },
            label = { Text("Validade Do Cartão") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = campo3,
            onValueChange = { novoTexto ->
                val textoFiltrado = novoTexto.filter { it.isDigit() }
                if (textoFiltrado.length <= 3) {
                    campo3 = textoFiltrado
                    validade = textoFiltrado.toIntOrNull() ?: 0
                }
            },
            label = { Text("Cvc Do Cartão") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val usuarioLogadoId = 1 // Exemplo: Substitua pela lógica real
                val novoCartao = Cartao(
                    banco = banco,
                    nome = nome,
                    numero = numero,
                    validade = validade,
                    cvc = cvc,
                    userId = usuarioLogadoId // Incluindo o ID do usuário
                )

                RetrofitClient.instance.addCartao(novoCartao).enqueue(object : Callback<Cartao> {

                    override fun onResponse(call: Call<Cartao>, response: Response<Cartao>) {
                        if (response.isSuccessful) {
                            val cartaoAdicionado = response.body()
                            Log.d("API_SUCCESS", "Cartão cadastrado com sucesso: ID ${cartaoAdicionado?.id}")
                            navController.navigate("cartoes")
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("API_ERROR", "Erro na resposta: ${response.code()} - $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<Cartao>, t: Throwable) {
                        Log.e("API_FAILURE", "Falha na comunicação: ${t.message}", t)
                    }
                })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cadastrar Cartão")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaCadastroCartaoPreview() {
    TelaCadastroCartao(navController = rememberNavController())
}
