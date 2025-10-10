package com.example.myapplication.remote

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

    // ==========================================================
    // NOVA FUNÇÃO PARA REGISTRAR ENTRADA (CHECK-IN)
    // ==========================================================
    @POST("sessoes/checkin")
    suspend fun registrarCheckIn(@Body checkInJson: JsonObject): Response<CheckInResponse>
}
