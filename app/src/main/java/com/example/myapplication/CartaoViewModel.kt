package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.CartaoResponse
import com.example.myapplication.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define os possíveis estados da tela
sealed class ListaCartoesState {
    object Loading : ListaCartoesState()
    data class Success(val cartoes: List<CartaoResponse>) : ListaCartoesState()
    data class Error(val message: String) : ListaCartoesState()
}

class CartaoViewModel : ViewModel() {

    private val _listaCartoesState = MutableStateFlow<ListaCartoesState>(ListaCartoesState.Loading)
    val listaCartoesState = _listaCartoesState.asStateFlow()

    fun buscarCartoes(context: Context) {
        viewModelScope.launch {
            _listaCartoesState.value = ListaCartoesState.Loading

            // 1. Pega o token salvo no SessionManager
            val token = SessionManager.getAuthToken(context)
            if (token == null) {
                _listaCartoesState.value = ListaCartoesState.Error("Usuário não autenticado.")
                return@launch
            }

            try {
                // 2. Chama a nova função da API, passando o token no Header
                val response = RetrofitClient.api.getMeusCartoes("Bearer $token")

                if (response.isSuccessful) {
                    // 3. Em caso de sucesso, atualiza o estado com a lista de cartões
                    _listaCartoesState.value = ListaCartoesState.Success(response.body() ?: emptyList())
                } else {
                    // 4. Se a resposta não for 2xx, trata como erro (incluindo 404)
                    val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido"
                    Log.e("BUSCAR_CARTOES", "Erro ${response.code()}: $errorMsg")
                    _listaCartoesState.value = ListaCartoesState.Error(errorMsg)
                }
            } catch (e: Exception) {
                // 5. Trata erros de conexão
                Log.e("BUSCAR_CARTOES", "Exceção: ${e.message}")
                _listaCartoesState.value = ListaCartoesState.Error("Falha na conexão com o servidor.")
            }
        }
    }
}
