package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.CartaoResponse
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados para a ação de CADASTRAR um cartão (já existente)
sealed class CadastroCartaoState {
    object Idle : CadastroCartaoState()
    object Loading : CadastroCartaoState()
    object Success : CadastroCartaoState()
    data class Error(val message: String) : CadastroCartaoState()
}

// NOVO: Estados para a ação de LISTAR os cartões na tela
sealed class ListaCartoesState {
    object Loading : ListaCartoesState()
    data class Success(val cartoes: List<CartaoResponse>) : ListaCartoesState()
    data class Error(val message: String) : ListaCartoesState()
}

class CartaoViewModel : ViewModel() {

    // --- LÓGICA DE CADASTRO (existente, sem alterações) ---
    private val _cadastroState = MutableStateFlow<CadastroCartaoState>(CadastroCartaoState.Idle)
    val cadastroState: StateFlow<CadastroCartaoState> = _cadastroState

    fun cadastrarCartao(context: Context, numero: String, nome: String, validade: String, cvv: String) {
        val userId = SessionManager.getUserId(context)
        if (userId == -1) {
            _cadastroState.value = CadastroCartaoState.Error("ID do usuário não encontrado.")
            return
        }
        viewModelScope.launch {
            _cadastroState.value = CadastroCartaoState.Loading
            try {
                val cartaoJson = JsonObject().apply {
                    addProperty("numero", numero)
                    addProperty("nome", nome)
                    addProperty("validade", validade)
                    addProperty("cvv", cvv)
                    addProperty("usuario_id", userId)
                }
                val response = RetrofitClient.api.cadastrarCartao(token = "Bearer ", cartaoJson = cartaoJson)
                if (response.isSuccessful) {
                    _cadastroState.value = CadastroCartaoState.Success
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido."
                    _cadastroState.value = CadastroCartaoState.Error(errorBody)
                }
            } catch (e: Exception) {
                _cadastroState.value = CadastroCartaoState.Error("Falha na conexão: ${e.message}")
            }
        }
    }

    fun resetState() {
        _cadastroState.value = CadastroCartaoState.Idle
    }
    // --- FIM DA LÓGICA DE CADASTRO ---

    // --- NOVA LÓGICA PARA BUSCAR CARTÕES ---
    private val _listaCartoesState = MutableStateFlow<ListaCartoesState>(ListaCartoesState.Loading)
    val listaCartoesState: StateFlow<ListaCartoesState> = _listaCartoesState.asStateFlow()

    fun buscarCartoes(context: Context) {
        val userId = SessionManager.getUserId(context)
        if (userId == -1) {
            _listaCartoesState.value = ListaCartoesState.Error("Usuário não autenticado.")
            return
        }

        viewModelScope.launch {
            _listaCartoesState.value = ListaCartoesState.Loading
            try {
                // Chama a nova função da API
                val response = RetrofitClient.api.getCartoesDoUsuario(userId)

                if (response.isSuccessful) {
                    // Se sucesso, atualiza o estado com a lista de cartões recebida
                    _listaCartoesState.value = ListaCartoesState.Success(response.body() ?: emptyList())
                } else {
                    val errorMsg = "Erro ao buscar cartões: ${response.code()}"
                    Log.e("CartaoViewModel", errorMsg)
                    _listaCartoesState.value = ListaCartoesState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Falha na conexão ao buscar cartões: ${e.message}"
                Log.e("CartaoViewModel", errorMsg)
                _listaCartoesState.value = ListaCartoesState.Error(errorMsg)
            }
        }
    }
}
