package com.example.myapplication

// Importe seus novos modelos e a ApiService
import android.util.Log
import com.example.myapplication.remote.ApiService
import com.example.myapplication.remote.PagamentoTicketRequest
import com.example.myapplication.model.ResumoPagamentoData
import com.example.myapplication.remote.CartaoResponse
import java.io.IOException

class PagamentoRepository(
    // Receba a ApiService aqui (idealmente via injeção de dependência)
    private val apiService: ApiService
) {

    /**
     * Efetua o pagamento chamando a API real.
     */
    suspend fun efetuarPagamento(dadosPagamento: ResumoPagamentoData): Result<String> {
        // 1. Crie o corpo da requisição com os dados necessários
        // Você precisará ter o sessao_id, cartao_id e o token do usuário logado
        val request = PagamentoTicketRequest(
            sessao_id = dadosPagamento.sessaoId, // Exemplo: adicione esse campo ao seu ResumoPagamentoData
            cartao_id = dadosPagamento.cartaoId, // Exemplo: adicione esse campo também
            valor_total = dadosPagamento.valorTotal
        )

        // Suponha que você tenha o token de autenticação
        val authToken = "Bearer "

        try {
            // 2. Execute a chamada de rede
            val response = apiService.efetuarPagamentoTicket(authToken, request)

            // 3. Trate a resposta da API
            return if (response.isSuccessful && response.body() != null) {
                val corpoResposta = response.body()!!
                // Sucesso: Retorna o ID da transação
                Result.success("ID Transação: ${corpoResposta.id_transacao}")
            } else {
                // Erro da API (ex: saldo insuficiente, cartão inválido)
                val erroMsg = response.errorBody()?.string() ?: "Erro na resposta da API."
                Result.failure(Exception(erroMsg))
            }
        } catch (e: IOException) {
            // Erro de rede (sem internet, etc.)
            return Result.failure(Exception("Falha de conexão. Verifique sua internet."))
        } catch (e: Exception) {
            // Outros erros inesperados
            return Result.failure(Exception("Ocorreu um erro inesperado: ${e.message}"))
        }
    }

    /**
     * Busca a lista de cartões cadastrados para um usuário específico.
     */
    suspend fun getCartoesDoUsuario(usuarioId: Int): Result<List<CartaoResponse>> {
        return try {
            val response = apiService.getCartoesDoUsuario(usuarioId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Trata erros da API, como usuário não encontrado ou não autorizado
                Result.failure(Exception("Erro ao buscar cartões: ${response.code()}"))
            }
        } catch (e: IOException) {
            // Trata erros de rede
            Result.failure(Exception("Falha de conexão. Verifique sua internet."))
        } catch (e: Exception) {
            // Trata outros erros inesperados
            Result.failure(Exception("Ocorreu um erro inesperado."))
        }
    }

    // Esta função tentará finalizar a sessão e retornará os dados ou null em caso de erro.
    suspend fun finalizarSessao(sessaoId: Int): ResumoPagamentoData? {
        try {
            val response = apiService.finalizarSessao(sessaoId)

            if (response.isSuccessful) {
                // Se a chamada foi bem-sucedida (código 2xx), retorna o corpo da resposta
                return response.body()
            } else {
                // Logar o erro, tratar códigos de erro específicos, etc.
                Log.e("PagamentoRepository", "Erro ao finalizar sessão: ${response.code()}")
                return null
            }
        } catch (e: Exception) {
            // Tratar exceções de rede (ex: sem internet)
            Log.e("PagamentoRepository", "Falha na conexão: ${e.message}")
            return null
        }
    }
}