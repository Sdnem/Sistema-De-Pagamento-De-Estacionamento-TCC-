package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.CheckInResponse
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Definição dos estados da UI
sealed class CheckInState {
    object Idle : CheckInState()
    object Loading : CheckInState()
    data class Success(val response: CheckInResponse) : CheckInState()
    data class Error(val message: String) : CheckInState()
}

class EstacionamentoViewModel : ViewModel() {

    private val _checkInState = MutableStateFlow<CheckInState>(CheckInState.Idle)
    val checkInState = _checkInState.asStateFlow()

    fun fazerCheckIn(context: Context) {
        // Evita múltiplas chamadas se já estiver carregando
        if (_checkInState.value == CheckInState.Loading) return

        _checkInState.value = CheckInState.Loading

        viewModelScope.launch {
            // ================================================
            // LÓGICA DE AUTENTICAÇÃO ADICIONADA AQUI
            // ================================================

            // 1. Buscar o token de autorização do SessionManager
            val token = SessionManager.getAuthToken(context)
            if (token == null) {
                // Se não houver token, o usuário não está logado.
                _checkInState.value = CheckInState.Error("Usuário não autenticado. Faça o login novamente.")
                return@launch
            }

            try {
                // 2. Chamar a API, passando o token no Header
                val response = RetrofitClient.api.registrarCheckIn("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    _checkInState.value = CheckInState.Success(response.body()!!)
                } else {
                    // Trata erros da API, como 409 (sessão já ativa) ou o 401 que você estava vendo
                    val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido no check-in."
                    Log.e("CHECKIN_VM", "Erro ${response.code()}: $errorMsg")
                    _checkInState.value = CheckInState.Error(errorMsg)
                }

            } catch (e: Exception) {
                // Trata erros de conexão (sem internet, etc.)
                Log.e("CHECKIN_VM", "Exceção: ${e.message}")
                _checkInState.value = CheckInState.Error("Falha na conexão com o servidor.")
            }
        }
    }

    // Função para resetar o estado após a navegação ou exibição do erro
    fun resetState() {
        _checkInState.value = CheckInState.Idle
    }
}
