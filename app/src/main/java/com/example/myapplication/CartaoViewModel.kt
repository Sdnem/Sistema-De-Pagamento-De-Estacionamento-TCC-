package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Cartao
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CartaoViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _cartoes = MutableStateFlow<List<Cartao>>(emptyList())
    val cartoes: StateFlow<List<Cartao>> = _cartoes

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    // ✅ NOVO: Usando SharedFlow para eventos de canal único
    private val _cadastroEvent = MutableSharedFlow<Unit>()
    val cadastroEvent = _cadastroEvent.asSharedFlow()

    init {
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
        RetrofitClient.getInstanceCartao(token).getCartoes().enqueue(object : Callback<List<Cartao>> {
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

    fun addCartao(novoCartao: Cartao) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            errorMessage.value = "Sessão expirada. Faça login novamente."
            return
        }

        // Inicia a corrotina no escopo do ViewModel
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                // 1. A chamada de rede agora é direta. O 'await' é implícito em funções suspend.
                val cartaoAdicionado = RetrofitClient.getInstanceCartao(token).addCartao(novoCartao)

                // 2. O código que estava no 'onResponse' bem-sucedido vem aqui.
                val listaAtual = _cartoes.value?.toMutableList() ?: mutableListOf()
                listaAtual.add(cartaoAdicionado)
                _cartoes.value = listaAtual

                // Emitindo o evento de sucesso
                _cadastroEvent.emit(Unit)

            } catch (e: HttpException) {
                // 3. Trata erros de resposta do servidor (ex: 404, 500, etc.)
                // Equivale à parte 'else' do onResponse.
                errorMessage.value = "Erro ao adicionar o cartão."

            } catch (e: IOException) {
                // 4. Trata erros de conexão (sem internet, timeout, etc.)
                // Equivale ao onFailure.
                errorMessage.value = "Falha na conexão: ${e.message}"

            } finally {
                // 5. O bloco 'finally' garante que o loading sempre será finalizado,
                // não importa se a chamada deu sucesso ou falhou.
                isLoading.value = false
            }
        }
    }

    fun deletarCartao(cartaoId: Int) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            errorMessage.value = "Sessão expirada. Faça login novamente."
            return
        }
        RetrofitClient.getInstanceCartao(token).deleteCartao(cartaoId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
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