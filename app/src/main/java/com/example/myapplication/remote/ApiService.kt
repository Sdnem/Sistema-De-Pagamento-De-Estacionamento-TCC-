package com.example.myapplication.remote

import com.example.myapplication.model.ResumoPagamentoData
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// Modelo de dados para a lista de cartões
data class CartaoResponse(
    val id: Int,
    val numero: String,
    val nome: String,
    val validade: String
)

// Modelo para enviar os dados do pagamento para a API
data class PagamentoTicketRequest(
    val sessao_id: Int,
    val cartao_id: Int,
    val valor_total: String
)

// Modelo para receber a resposta de sucesso da API
data class PagamentoTicketResponse(
    val status: String,
    val id_transacao: String,
    val mensagem: String
)

// NOVO: Modelo de dados para a resposta do check-in
data class CheckInResponse(
    val status: String,
    val mensagem: String,
    val sessao_id: Int,
    val horario_entrada: String // Formato ISO, ex: "2024-10-27T10:00:00"
)

interface ApiService {

    @POST("usuarios/cadastrar")
    suspend fun cadastrarUsuario(@Body usuarioJson: JsonObject): Response<JsonObject>

    @POST("usuarios/login")
    suspend fun login(@Body loginRequest: JsonObject): Response<JsonObject>

    @POST("cartoes/cadastrar")
    suspend fun cadastrarCartao(
        @Header("Authorization") token: String,
        @Body cartaoJson: JsonObject
    ): Response<JsonObject>

    @GET("cartoes/{usuario_id}")
    suspend fun getCartoesDoUsuario(
        @Path("usuario_id") usuarioId: Int
    ): Response<List<CartaoResponse>>

    @POST("sessoes/checkin")
    suspend fun registrarCheckIn(@Body checkInJson: JsonObject): Response<CheckInResponse>

    @POST("pagamentos/ticket")
    suspend fun efetuarPagamentoTicket(
        @Header("Authorization") token: String, // Pagamentos geralmente são rotas protegidas
        @Body pagamentoRequest: PagamentoTicketRequest
    ): Response<PagamentoTicketResponse>

    // Exemplo: Finaliza uma sessão enviando o ID dela
    // A anotação @POST indica o método HTTP e o endpoint relativo
    @POST("sessoes/{id}/finalizar")
    suspend fun finalizarSessao(
        @Path("id") sessaoId: Int
    ): Response<ResumoPagamentoData> // Retorna o objeto diretamente
}
