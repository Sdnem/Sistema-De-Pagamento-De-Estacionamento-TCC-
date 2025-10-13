package com.example.myapplication.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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

// Modelo para a resposta da listagem de cartões.
data class CartaoResponse(
    val id: Int,
    val numero: String,
    val nome: String,
    val validade: String
)

// Modelo para a resposta do Check-in.
data class CheckInResponse(
    val status: String,
    val sessao_id: Int,
    val horario_entrada: String // Formato ISO, ex: "2024-10-27T10:00:00"
)

// ========================================================
// NOVO MODELO ADICIONADO AQUI
// ========================================================
// Modelo para a resposta do Checkout.
data class CheckOutResponse(
    val status: String,
    val mensagem: String,
    val valor_pago: Float
)
// ========================================================

/**
 * Interface que define todos os endpoints da API para o Retrofit.
 */
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

    @GET("cartoes")
    suspend fun getMeusCartoes(
        @Header("Authorization") token: String
    ): Response<List<CartaoResponse>>

    @POST("sessoes/checkin")
    suspend fun registrarCheckIn(
        @Header("Authorization") token: String,
        @Body checkInJson: JsonObject = JsonObject() // Envia corpo vazio por padrão
    ): Response<CheckInResponse>

    // ========================================================
    // NOVA FUNÇÃO DE API ADICIONADA AQUI
    // ========================================================
    @POST("sessoes/checkout")
    suspend fun registrarCheckout(
        @Header("Authorization") token: String
    ): Response<CheckOutResponse>
    // ========================================================
}
