package com.example.myapplication.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // URL base da sua API.
    // 10.0.2.2 é o endereço especial que o emulador Android usa para se conectar ao localhost do seu computador.
    private const val BASE_URL = "https://app-tcc-backend.onrender.com/"

    // Instância única e "preguiçosa" (lazy) do Retrofit.
    // Ela só será criada na primeira vez que for usada.
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instância única da nossa interface de API.
    val api: ApiService by lazy {
        instance.create(ApiService::class.java)
    }
}
