package com.example.myapplication.remote

import com.example.myapplication.model.Usuario
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("usuarios/cadastrar")
    fun cadastrar(@Body usuario: Usuario): Call<ResponseBody>

    @POST("usuarios/login")
    fun login(@Body usuario: Usuario): Call<Map<String, String>>

}