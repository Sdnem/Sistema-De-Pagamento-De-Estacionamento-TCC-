package com.example.myapplication.remote

import com.example.myapplication.model.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/api/" // Ajuste para o endpoint da API

    // Função para criar uma instância com o token
    fun getInstanceCartao(token: String): CartaoApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(CartaoApi::class.java)
    }

    fun getInstanceCarro(token: String): CarroApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(CarroApi::class.java)
    }
}