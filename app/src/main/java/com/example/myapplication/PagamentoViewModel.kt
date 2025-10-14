package com.example.myapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.ResumoPagamentoData
import com.example.myapplication.model.SessionManager
import com.example.myapplication.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Sealed Interface para representar os estados da UI de forma segura e explícita.
sealed interface PagamentoUiState {
    // Estado inicial, aguardando os dados do resumo
    object Ocioso : PagamentoUiState

    // Contém os dados necessários para exibir a tela de resumo.
    data class ProntoParaConfirmar(val resumo: ResumoPagamentoData) : PagamentoUiState

    // Estado durante o processamento do pagamento (ex: mostrando um loader).
    object Carregando : PagamentoUiState

    // Estado de sucesso após o pagamento ser confirmado.
    // Contém o valor total para ser exibido na tela de sucesso
    data class Sucesso(val valorConfirmado: String) : PagamentoUiState

    // Estado de falha, com uma mensagem específica sobre o erro.
    data class Falha(val mensagemErro: String, val erroEspecifico: String? = null) : PagamentoUiState
}

class PagamentoViewModel(
    apiService: ApiService,
    // Injetar o repositório facilita os testes e a manutenção.
    private val pagamentoRepository: PagamentoRepository = PagamentoRepository(apiService)
) : ViewModel() {

    // StateFlow privado para conter o estado da UI.
    private val _uiState = MutableStateFlow<PagamentoUiState?>(null)

    // StateFlow público e somente leitura para a UI observar.
    val uiState: StateFlow<PagamentoUiState?> = _uiState.asStateFlow()

    private var dadosDoPagamentoAtual: ResumoPagamentoData? = null

    /**
     * Prepara o ViewModel com os dados do pagamento para iniciar o fluxo.
     * Deve ser chamado ao navegar para a tela de resumo.
     */
    fun iniciarFluxoDePagamento(resumoData: ResumoPagamentoData) {
        dadosDoPagamentoAtual = resumoData
        _uiState.value = PagamentoUiState.ProntoParaConfirmar(resumoData)
    }

    /**
     * Função principal chamada pela UI para iniciar o processo de pagamento.
     */
    fun processarPagamento(context: Context) {
        // Garante que temos os dados necessários antes de prosseguir
        val dadosAtuais = dadosDoPagamentoAtual ?: return

        // Inicia a coroutine no escopo do ViewModel
        viewModelScope.launch {
            // 1. Atualiza o estado para Carregando
            _uiState.value = PagamentoUiState.Carregando

            // 2. Chama o repositório para efetuar o pagamento
            val resultado = pagamentoRepository.efetuarPagamento(dadosAtuais)

            // 3. Atualiza o estado com base no resultado
            resultado.fold(
                onSuccess = {
                    // Em caso de sucesso
                    _uiState.value = PagamentoUiState.Sucesso(valorConfirmado = dadosAtuais.valorTotal)

                    // AQUI: Limpa o estado de check-in após o pagamento!
                    SessionManager.setCheckInStatus(context, false)
                },
                onFailure = { erro ->
                    // Em caso de falha
                    _uiState.value = PagamentoUiState.Falha(
                        mensagemErro = "Não foi possível processar seu pagamento. Verifique os dados e tente novamente.",
                        erroEspecifico = "Motivo: ${erro.message}"
                    )
                }
            )
        }
    }

    /**
     * Reseta o estado para permitir uma nova tentativa de pagamento.
     * Deve ser chamado ao clicar em "TENTAR NOVAMENTE" na tela de falha.
     */
    fun tentarNovamente() {
        dadosDoPagamentoAtual?.let {
            _uiState.value = PagamentoUiState.ProntoParaConfirmar(it)
        }
    }
}