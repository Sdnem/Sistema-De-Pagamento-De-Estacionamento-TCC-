package com.example.myapplication.remote

import com.example.myapplication.model.ResumoPagamentoData
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// Modelo para a resposta do Login
// Contém informações do usuário e o status da sessão de estacionamento.
data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val user_id: Int,
    val user_name: String,
    val card_count: Int,
    val active_session_info: ActiveSessionInfo? // Pode ser nulo se não houver sessão
)

// Sub-modelo para a informação da sessão ativa dentro do LoginResponse.
data class ActiveSessionInfo(
    val sessao_id: Int,
    val horario_entrada: String
)

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

data class CheckOutResponse(
    val status: String,
    val mensagem: String,
    val valor_pago: Float
)

interface ApiService {

    @POST("usuarios/cadastrar")
    suspend fun cadastrarUsuario(@Body usuarioJson: JsonObject): Response<JsonObject>

    @FormUrlEncoded
    @POST("usuarios/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") senha: String
    ): Response<LoginResponse>

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

    @POST("sessoes/checkin")
    suspend fun registrarCheckIn(
        @Header("Authorization") token: String,
        @Body checkInJson: JsonObject = JsonObject() // Envia corpo vazio por padrão
    ): Response<CheckInResponse>

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

    @POST("sessoes/checkout")
    suspend fun registrarCheckout(
        @Header("Authorization") token: String
    ): Response<CheckOutResponse>
}
