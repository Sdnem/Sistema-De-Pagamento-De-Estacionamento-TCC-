package com.example.myapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsável por gerenciar o estado da sessão de estacionamento.
 *
 * Sua principal responsabilidade é verificar proativamente se já existe
 * uma sessão ativa no backend quando o app é iniciado ou quando a HomeScreen
 * é carregada.
 */
class EstacionamentoViewModel : ViewModel() {

    // Armazena a informação da sessão ativa (o horário de entrada).
    // O valor é nulo se não houver sessão ativa.
    // É "privado" para ser modificado apenas dentro do ViewModel.
    private val _activeSessionInfo = MutableStateFlow<String?>(null)

    // É "público" e imutável para a UI (HomeScreen) apenas observar.
    val activeSessionInfo: StateFlow<String?> = _activeSessionInfo

    /**
     * Verifica ativamente no servidor se o usuário logado possui uma sessão de estacionamento.
     * Esta função deve ser chamada pela HomeScreen sempre que ela for carregada.
     */
    fun checkForActiveSession(context: Context) {
        // Evita chamadas repetidas se já estivermos cientes de uma sessão.
        if (_activeSessionInfo.value != null) {
            return
        }

        viewModelScope.launch {
            // Primeiro, precisamos do token de autenticação para fazer a chamada.
            val token = SessionManager.getAuthToken(context)
            if (token == null) {
                // Se não há token, não pode haver sessão. Garante que o estado está limpo.
                _activeSessionInfo.value = null
                return@launch
            }

            try {
                // Usa a rota GET /sessoes/status para verificar o estado no backend.
                val response = RetrofitClient.api.verificarSessaoAtiva("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    // SUCESSO! Uma sessão ativa foi encontrada.
                    // Armazena o horário de entrada no nosso StateFlow.
                    _activeSessionInfo.value = response.body()!!.horario_entrada
                } else {
                    // Se a resposta não foi bem-sucedida (ex: 404 Not Found),
                    // significa que não há sessão ativa. Limpa o estado.
                    _activeSessionInfo.value = null
                }
            } catch (e: Exception) {
                // Em caso de erro de rede, assume que não há sessão e limpa o estado.
                // Poderíamos adicionar um estado de erro para a UI aqui se quiséssemos.
                _activeSessionInfo.value = null
            }
        }
    }

    /**
     * Limpa o estado da sessão ativa.
     * Deve ser chamado ao iniciar o processo de checkout na TelaEstacionamento
     * para prevenir que a HomeScreen navegue de volta para a sessão que está sendo encerrada.
     */
    fun clearActiveSession() {
        _activeSessionInfo.value = null
    }

    /**
     * Função para a UI "consumir" o evento de navegação e limpar o estado.
     * Isso evita que a navegação para a TelaEstacionamento ocorra novamente
     * se a HomeScreen for recomposta por algum motivo.
     */
    fun onNavigationHandled() {
        _activeSessionInfo.value = null
    }
}
