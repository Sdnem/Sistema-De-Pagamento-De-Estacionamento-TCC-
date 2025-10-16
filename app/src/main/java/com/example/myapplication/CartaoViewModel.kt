package com.example.myapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.CartaoResponse
import com.example.myapplication.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define os possíveis estados da UI para a lista de cartões
sealed class ListaCartoesState {
    object Loading : ListaCartoesState() // Estado de carregamento
    data class Success(val cartoes: List<CartaoResponse>) : ListaCartoesState() // Sucesso, com a lista de cartões
    data class Error(val message: String) : ListaCartoesState() // Ocorreu um erro
}

// Classe de estado genérica para operações (como definir padrão ou excluir)
sealed class OperationState<out T> {
    object Idle : OperationState<Nothing>() // Estado inicial, ocioso
    object Loading : OperationState<Nothing>() // Operação em andamento
    data class Success<T>(val data: T) : OperationState<T>() // Operação bem-sucedida
    data class Error(val message: String) : OperationState<Nothing>() // Falha na operação
}

/**
 * ViewModel responsável pela lógica da tela de listagem de cartões.
 */
class CartaoViewModel : ViewModel() {

    // Estado para a lista de cartões
    private val _listaCartoesState = MutableStateFlow<ListaCartoesState>(ListaCartoesState.Loading)
    val listaCartoesState: StateFlow<ListaCartoesState> = _listaCartoesState.asStateFlow()

    // Estado para a operação de definir cartão padrão
    private val _definirPadraoState = MutableStateFlow<OperationState<String>>(OperationState.Idle)
    val definirPadraoState: StateFlow<OperationState<String>> = _definirPadraoState.asStateFlow()

    // ========================================================
    //        NOVO ESTADO PARA EXCLUSÃO DE CARTÃO
    // ========================================================
    // Estado para refletir o resultado da operação de excluir cartão
    private val _excluirCartaoState = MutableStateFlow<OperationState<String>>(OperationState.Idle)
    val excluirCartaoState: StateFlow<OperationState<String>> = _excluirCartaoState.asStateFlow()


    /**
     * Busca a lista de cartões do usuário logado na API.
     */
    fun buscarCartoes(context: Context) {
        viewModelScope.launch {
            _listaCartoesState.value = ListaCartoesState.Loading
            // Reseta os estados de operação para evitar que mensagens antigas apareçam
            _definirPadraoState.value = OperationState.Idle
            _excluirCartaoState.value = OperationState.Idle

            val token = SessionManager.getAuthToken(context)
            if (token == null) {
                _listaCartoesState.value = ListaCartoesState.Error("Sessão expirada. Faça login novamente.")
                return@launch
            }

            try {
                val response = RetrofitClient.api.getMeusCartoes("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _listaCartoesState.value = ListaCartoesState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erro ao buscar cartões."
                    _listaCartoesState.value = ListaCartoesState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _listaCartoesState.value = ListaCartoesState.Error("Falha na conexão: ${e.message}")
            }
        }
    }

    /**
     * Define um cartão específico como o método de pagamento padrão do usuário.
     */
    fun definirComoPadrao(context: Context, cartaoId: Int) {
        viewModelScope.launch {
            _definirPadraoState.value = OperationState.Loading
            val token = SessionManager.getAuthToken(context)
            if (token == null) {
                _definirPadraoState.value = OperationState.Error("Sessão expirada.")
                return@launch
            }

            try {
                val response = RetrofitClient.api.definirCartaoPadrao("Bearer $token", cartaoId)
                if (response.isSuccessful) {
                    _definirPadraoState.value = OperationState.Success("Cartão definido como padrão!")
                    // Após o sucesso, recarrega a lista para a UI refletir a mudança.
                    buscarCartoes(context)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Não foi possível definir como padrão."
                    _definirPadraoState.value = OperationState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _definirPadraoState.value = OperationState.Error("Falha na conexão: ${e.message}")
            }
        }
    }

    /**
     * Reseta o estado da operação 'definir padrão' para Idle.
     */
    fun resetDefinirPadraoState() {
        _definirPadraoState.value = OperationState.Idle
    }


    // ========================================================
    //        NOVAS FUNÇÕES PARA EXCLUSÃO DE CARTÃO
    // ========================================================
    /**
     * Exclui um cartão específico do usuário.
     */
    fun excluirCartao(context: Context, cartaoId: Int) {
        viewModelScope.launch {
            _excluirCartaoState.value = OperationState.Loading
            val token = SessionManager.getAuthToken(context)
            if (token == null) {
                _excluirCartaoState.value = OperationState.Error("Sessão expirada.")
                return@launch
            }

            try {
                val response = RetrofitClient.api.excluirCartao("Bearer $token", cartaoId)
                if (response.isSuccessful) {
                    _excluirCartaoState.value = OperationState.Success("Cartão excluído com sucesso!")
                    // IMPORTANTE: Após excluir, também recarregamos a lista de cartões.
                    buscarCartoes(context)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Não foi possível excluir o cartão."
                    _excluirCartaoState.value = OperationState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _excluirCartaoState.value = OperationState.Error("Falha na conexão: ${e.message}")
            }
        }
    }

    /**
     * Reseta o estado da operação 'excluir cartão' para Idle.
     */
    fun resetExcluirCartaoState() {
        _excluirCartaoState.value = OperationState.Idle
    }
}
