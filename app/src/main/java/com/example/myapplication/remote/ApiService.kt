package com.example.myapplication.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.*

// ========================================================
//              MODELOS DE DADOS (DATA CLASSES)
// ========================================================

/**
 * Resposta do login. Contém o token e informações da sessão,
 * se houver uma ativa para este usuário.
 */
data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val user_id: Int,
    val user_name: String,
    val card_count: Int,
    val active_session_info: ActiveSessionInfo? // Nulo se não houver sessão ativa
)

/**
 * Sub-modelo com os detalhes da sessão ativa.
 */
data class ActiveSessionInfo(
    val sessao_id: Int,
    val horario_entrada: String
)

/**
 * Modelo para um cartão individual na lista de cartões do usuário.
 */
data class CartaoResponse(
    val id: Int,
    val numero: String, // O backend deve retornar o número já mascarado
    val nome: String,
    val validade: String,
    val is_default: Boolean,
    val bandeira: String
)

/**
 * Resposta da API ao fazer o check-in (iniciar uma sessão).
 */
data class CheckInResponse(
    val status: String,
    val sessao_id: Int,
    val horario_entrada: String // Formato ISO, ex: "2024-10-27T10:00:00"
)

/**
 * Resposta da API ao fazer o checkout (finalizar e pagar uma sessão).
 */
data class CheckOutResponse(
    val status: String,
    val mensagem: String,
    val valor_pago: Float
)

// ========================================================
//              NOVO DATA CLASS ADICIONADO AQUI
// ========================================================
/**
 * Resposta da API (simulada) de horários de pico.
 * Usado na HomeScreen para exibir a movimentação atual.
 */
data class HorariosPicoResponse(
    val status_movimento_atual: String,
    val lotacao_percentual_atual: Int
)


// ========================================================
//              INTERFACE DA API (ENDPOINTS)
// ========================================================

/**
 * Interface que define todos os endpoints da API para o Retrofit.
 * Cada função corresponde a uma rota do backend.
 */
interface ApiService {

    /**
     * Rota para registrar um novo usuário.
     */
    @POST("usuarios/cadastrar")
    suspend fun cadastrarUsuario(@Body usuarioJson: JsonObject): Response<JsonObject>

    /**
     * Rota para autenticar um usuário e obter um token de acesso.
     */
    @FormUrlEncoded
    @POST("usuarios/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") senha: String
    ): Response<LoginResponse>

    /**
     * Rota para cadastrar um novo cartão de crédito para o usuário logado.
     * Requer um token de autorização.
     */
    @POST("cartoes/cadastrar")
    suspend fun cadastrarCartao(
        @Header("Authorization") token: String,
        @Body cartaoJson: JsonObject
    ): Response<JsonObject>

    /**
     * Rota para buscar a lista de cartões do usuário logado.
     * Requer um token de autorização.
     */
    @GET("cartoes")
    suspend fun getMeusCartoes(
        @Header("Authorization") token: String
    ): Response<List<CartaoResponse>>

    /**
     * Rota para definir um cartão específico como o método de pagamento padrão.
     * Requer um token de autorização.
     */
    @POST("cartoes/{id}/definir-padrao")
    suspend fun definirCartaoPadrao(
        @Header("Authorization") token: String,
        @Path("id") cartaoId: Int
    ): Response<Unit>

    /**
     * Rota para excluir um cartão específico do usuário.
     * Requer um token de autorização.
     */
    @DELETE("cartoes/{id}")
    suspend fun excluirCartao(
        @Header("Authorization") token: String,
        @Path("id") cartaoId: Int
    ): Response<Unit>

    /**
     * Rota para iniciar uma nova sessão de estacionamento (check-in).
     */
    @POST("sessoes/checkin")
    suspend fun registrarCheckIn(
        @Header("Authorization") token: String,
        @Body checkInJson: JsonObject
    ): Response<CheckInResponse>

    /**
     * Rota para finalizar e pagar a sessão de estacionamento ativa (checkout).
     * Requer um token de autorização.
     */
    @POST("sessoes/checkout")
    suspend fun registrarCheckout(
        @Header("Authorization") token: String
    ): Response<CheckOutResponse>

    /**
     * Rota para verificar se o usuário logado possui uma sessão de estacionamento ativa.
     */
    @GET("sessoes/status")
    suspend fun verificarSessaoAtiva(
        @Header("Authorization") token: String
    ): Response<ActiveSessionInfo>

    /**
     * Rota para buscar uma prévia do valor de checkout da sessão ativa, sem finalizá-la.
     * Usada na TelaEstacionamento para exibir o valor atualizado do servidor.
     */
    @GET("sessoes/checkout/preview")
    suspend fun preverValorCheckout(
        @Header("Authorization") token: String
    ): Response<JsonObject>

    // ========================================================
    //              NOVA FUNÇÃO ADICIONADA AQUI
    // ========================================================
    /**
     * Rota (simulada) para buscar os horários de pico do estabelecimento.
     * Não requer autenticação.
     */
    @GET("estabelecimento/horarios-pico")
    suspend fun getHorariosPico(): Response<HorariosPicoResponse>
}

