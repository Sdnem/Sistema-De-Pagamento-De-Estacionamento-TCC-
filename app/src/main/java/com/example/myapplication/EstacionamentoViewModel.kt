package com.example.myapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.ResumoPagamentoData
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.CheckInResponse
import com.example.myapplication.remote.RetrofitClient
import com.example.myapplication.PagamentoRepository
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define os possíveis estados da operação de Check-in
sealed class CheckInState {
    object Idle : CheckInState() // Estado inicial ou após reset
    object Loading : CheckInState() // Operação em andamento
    data class Success(val response: CheckInResponse) : CheckInState() // Sucesso
    data class Error(val message: String) : CheckInState() // Falha
}

class EstacionamentoViewModel(
    private val pagamentoRepository: PagamentoRepository
) : ViewModel() {

    private val _checkInState = MutableStateFlow<CheckInState>(CheckInState.Idle)
    val checkInState: StateFlow<CheckInState> = _checkInState

    // Novo StateFlow para o status de check-in ativo
    private val _isCheckInActive = MutableStateFlow(false)
    val isCheckInActive: StateFlow<Boolean> = _isCheckInActive.asStateFlow()

    /**
     * Verifica o status de check-in inicial ao criar a ViewModel.
     */
    fun verificarStatusCheckIn(context: Context) {
        _isCheckInActive.value = SessionManager.getCheckInStatus(context)
    }

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

                    // AQUI: Salva o estado de check-in como ativo!
                    SessionManager.setCheckInStatus(context, true)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _checkInState.value = CheckInState.Error(errorBody)
                }
            } catch (e: Exception) {
                _checkInState.value = CheckInState.Error("Falha na conexão: ${e.message}")
            }
        }
    }

    // 1. Crie um StateFlow para controlar o evento de navegação
    private val _navigateToResumo = MutableStateFlow<ResumoPagamentoData?>(null)
    val navigateToResumo: StateFlow<ResumoPagamentoData?> = _navigateToResumo.asStateFlow()

    // Estados para a UI (Boas práticas)
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // NOVO: StateFlow para armazenar o ID da sessão ativa
    private val _sessaoIdAtiva = MutableStateFlow<Int?>(null)
    val sessaoIdAtiva: StateFlow<Int?> = _sessaoIdAtiva

    // NOVO: StateFlow para sinalizar que a navegação para o resumo deve ocorrer
    private val _eventoDeNavegacaoResumo = MutableStateFlow<ResumoPagamentoData?>(null)
    val eventoDeNavegacaoResumo: StateFlow<ResumoPagamentoData?> = _eventoDeNavegacaoResumo

    fun finalizarSessaoEPreprarPagamento(sessaoId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val dadosDaSessao = pagamentoRepository.finalizarSessao(sessaoId)
            if (dadosDaSessao != null) {
                // SUCESSO: Emite os dados para o StateFlow
                _eventoDeNavegacaoResumo.value = dadosDaSessao
            } else {
                _error.value = "Falha ao obter dados da sessão."
            }
            _isLoading.value = false
        }
    }

    // Função para a UI chamar depois que a navegação for tratada
    fun onNavegacaoParaResumoFeita() {
        _eventoDeNavegacaoResumo.value = null
    }

    // Função para a UI "consumir" o evento de erro e limpar o estado
    fun onErrorShown() {
        _error.value = null
    }

    // Função para a UI "consumir" o evento de navegação e limpar o estado
    fun onNavigationDone() {
        _navigateToResumo.value = null
    }

    /**
     * Reseta o estado para Idle, permitindo uma nova operação.
     */
    fun resetState() {
        _checkInState.value = CheckInState.Idle
    }
}
