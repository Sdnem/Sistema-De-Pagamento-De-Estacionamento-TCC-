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

interface ApiService {
    @POST("usuarios/cadastrar")
    fun cadastrar(@Body usuario: Usuario): Call<ResponseBody>

    @POST("usuarios/login")
    fun login(@Body usuario: Usuario): Call<Map<String, String>>

}

interface CartaoApi {
    @GET("cartoes/{userId}")
    fun getCartoes(@Path("userId") userId: Int): Call<List<Cartao>>

    @POST("cartoes/{userID}")
    fun addCartao(@Body cartao: Cartao): Call<Cartao>

    @DELETE("cartoes/{cartaoId}")
    fun deleteCartao(@Path("cartaoId") cartaoId: Int): Call<Void>
}