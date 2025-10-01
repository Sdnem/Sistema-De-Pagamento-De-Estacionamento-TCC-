package com.example.myapplication.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/" // Troque pela sua URL base

    // Cliente OkHttp sem autenticação
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Função que cria um cliente Retrofit com token de autenticação
    fun getInstance(token: String?): ApiService {
        val httpClient = OkHttpClient.Builder()

        // Adiciona o Interceptor apenas se o token não for nulo
        token?.let {
            httpClient.addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $it")
                    .build()
                chain.proceed(newRequest)
            }
        }

        val retrofitInstance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofitInstance.create(ApiService::class.java)
    }

    // Instância para chamadas que não precisam de autenticação (ex: login)
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
