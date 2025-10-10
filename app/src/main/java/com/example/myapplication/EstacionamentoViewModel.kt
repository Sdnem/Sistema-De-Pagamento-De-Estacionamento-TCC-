package com.example.myapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.CheckInResponse
import com.example.myapplication.remote.RetrofitClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define os possíveis estados da operação de Check-in
sealed class CheckInState {
    object Idle : CheckInState() // Estado inicial ou após reset
    object Loading : CheckInState() // Operação em andamento
    data class Success(val response: CheckInResponse) : CheckInState() // Sucesso
    data class Error(val message: String) : CheckInState() // Falha
}

class EstacionamentoViewModel : ViewModel() {

    private val _checkInState = MutableStateFlow<CheckInState>(CheckInState.Idle)
    val checkInState: StateFlow<CheckInState> = _checkInState

    /**
     * Inicia o processo de check-in, comunicando-se com a API.
     */
    fun fazerCheckIn(context: Context) {
        val userId = SessionManager.getUserId(context)
        if (userId == -1) {
            _checkInState.value = CheckInState.Error("Usuário não autenticado.")
            return
        }

        viewModelScope.launch {
            _checkInState.value = CheckInState.Loading
            try {
                val requestBody = JsonObject().apply {
                    addProperty("usuario_id", userId)
                }
                val response = RetrofitClient.api.registrarCheckIn(requestBody)

                if (response.isSuccessful && response.body() != null) {
                    _checkInState.value = CheckInState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _checkInState.value = CheckInState.Error(errorBody)
                }
            } catch (e: Exception) {
                _checkInState.value = CheckInState.Error("Falha na conexão: ${e.message}")
            }
        }
    }

    /**
     * Reseta o estado para Idle, permitindo uma nova operação.
     */
    fun resetState() {
        _checkInState.value = CheckInState.Idle
    }
}
