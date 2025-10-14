package com.example.myapplication

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class CadastroViewModel : ViewModel() {
    private val apiService = RetrofitClient.api

    // Estado para a UI saber se o cadastro foi bem-sucedido
    var cadastroSucesso by mutableStateOf(false)
        private set

    fun cadastrarUsuario(nome: String, email: String, senha: String) {
        viewModelScope.launch {
            try {
                val usuarioJson = JsonObject().apply {
                    addProperty("nome", nome)
                    addProperty("email", email)
                    addProperty("senha", senha)
                }

                val response = apiService.cadastrarUsuario(usuarioJson)

                if (response.isSuccessful) {
                    Log.d("CadastroViewModel", "Usuário cadastrado com sucesso: ${response.body()}")
                    cadastroSucesso = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CadastroViewModel", "Erro ao cadastrar: $errorBody")
                    cadastroSucesso = false
                }
            } catch (e: Exception) {
                Log.e("CadastroViewModel", "Exceção durante o cadastro: ${e.message}")
                cadastroSucesso = false
            }
        }
    }

    // Função para resetar o estado após a navegação
    fun onNavegacaoCompleta() {
        cadastroSucesso = false
    }
}
