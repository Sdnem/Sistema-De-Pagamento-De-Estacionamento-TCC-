package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.remote.CartaoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define os possíveis estados da UI para a tela de seleção de cartão
sealed interface SelecaoCartaoUiState {
    object Carregando : SelecaoCartaoUiState
    data class Sucesso(val cartoes: List<CartaoResponse>) : SelecaoCartaoUiState
    data class Falha(val mensagem: String) : SelecaoCartaoUiState
}

class SelecaoCartaoViewModel(
    private val repository: PagamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SelecaoCartaoUiState>(SelecaoCartaoUiState.Carregando)
    val uiState: StateFlow<SelecaoCartaoUiState> = _uiState.asStateFlow()

    fun carregarCartoes(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.value = SelecaoCartaoUiState.Carregando
            val resultado = repository.getCartoesDoUsuario(usuarioId)
            resultado.fold(
                onSuccess = { cartoes ->
                    if (cartoes.isNotEmpty()) {
                        _uiState.value = SelecaoCartaoUiState.Sucesso(cartoes)
                    } else {
                        _uiState.value = SelecaoCartaoUiState.Falha("Nenhum cartão cadastrado.")
                    }
                },
                onFailure = { erro ->
                    _uiState.value = SelecaoCartaoUiState.Falha(erro.message ?: "Não foi possível carregar os cartões.")
                }
            )
        }
    }
}