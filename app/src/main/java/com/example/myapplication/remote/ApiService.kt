package com.example.myapplication.remote

import com.example.myapplication.model.Cartao
import com.example.myapplication.model.Usuario
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Interface unificada para todos os endpoints da API
interface ApiService {

    // --- Endpoints de Usuário ---

    @POST("usuarios/cadastrar")
    fun cadastrar(@Body usuario: Usuario): Call<ResponseBody>

    @POST("usuarios/login")
    fun login(@Body usuario: Usuario): Call<Map<String, String>>


    // --- Endpoints de Cartão (movidos para cá) ---

    // O backend obterá o usuário autenticado pelo token
    @GET("cartoes")
    fun getCartoes(): Call<List<Cartao>>

    // O backend obterá o userID do token, não precisa enviar na URL
    @POST("cartoes")
    fun addCartao(@Body cartao: Cartao): Call<Cartao>

    // O ID do cartão a ser deletado é passado na URL
    @DELETE("cartoes/{cartaoId}")
    fun deleteCartao(@Path("cartaoId") cartaoId: Int): Call<Void>
}
