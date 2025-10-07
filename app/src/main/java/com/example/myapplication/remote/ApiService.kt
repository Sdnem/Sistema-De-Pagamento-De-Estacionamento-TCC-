package com.example.myapplication.remote

import com.example.myapplication.model.Carro
import com.example.myapplication.model.Cartao
import com.example.myapplication.model.Usuario
import okhttp3.ResponseBody
import retrofit2.Response
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
    // O backend obterá o usuário pelo token
    @GET("cartoes")
    fun getCartoes(): Call<List<Cartao>>

    // O backend obterá o userID do token, não precisa enviar na URL
    @POST("cartoes")
    suspend fun addCartao(@Body novoCartao: Cartao): Cartao

    // O ID do cartão a ser deletado é passado na URL
    @DELETE("cartoes/{cartaoId}")
    fun deleteCartao(@Path("cartaoId") cartaoId: Int): Call<Void>
}

interface CarroApi {
    // O backend obterá o usuário pelo token
    @GET("carros")
    fun getCarro(): Call<List<Carro>>

    // O backend obterá o userID do token, não precisa enviar na URL
    @POST("carros")
    suspend fun addCarro(@Body novoCarro: Carro): Carro

    // O ID do cartão a ser deletado é passado na URL
    @DELETE("carro/{carroId}")
    fun deleteCarro(@Path("carroId") carroId: Int): Call<Void>
}