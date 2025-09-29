package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Cartao
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartaoViewModel(private val sessionManager: SessionManager) : ViewModel() {

    // StateFlow para expor a lista de cartões para a UI
    private val _cartoes = MutableStateFlow<List<Cartao>>(emptyList())
    val cartoes: StateFlow<List<Cartao>> = _cartoes

    // Estado para controlar o loading
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    init {
        // Carrega os cartões assim que a ViewModel é criada
        buscarCartoesDoUsuario()
    }

    fun buscarCartoesDoUsuario() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            errorMessage.value = "Usuário não autenticado."
            return
        }

        isLoading.value = true
        errorMessage.value = null

        // Usa a instância do Retrofit que inclui o token
        RetrofitClient.getInstance(token).getCartoes().enqueue(object : Callback<List<Cartao>> {
            override fun onResponse(call: Call<List<Cartao>>, response: Response<List<Cartao>>) {
                if (response.isSuccessful) {
                    _cartoes.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Erro ao buscar cartões."
                }
                isLoading.value = false
            }

            override fun onFailure(call: Call<List<Cartao>>, t: Throwable) {
                errorMessage.value = "Falha na conexão: ${t.message}"
                isLoading.value = false
            }
        })
    }

    fun deletarCartao(cartaoId: Int) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            errorMessage.value = "Sessão expirada. Faça login novamente."
            return
        }

        // Chama a API para deletar o cartão
        RetrofitClient.getInstance(token).deleteCartao(cartaoId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Se a remoção na API foi bem-sucedida, atualiza a lista localmente
                    val listaAtualizada = _cartoes.value.toMutableList()
                    listaAtualizada.removeAll { it.id == cartaoId }
                    _cartoes.value = listaAtualizada
                } else {
                    errorMessage.value = "Não foi possível remover o cartão."
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                errorMessage.value = "Falha na conexão: ${t.message}"
            }
        })
    }
}