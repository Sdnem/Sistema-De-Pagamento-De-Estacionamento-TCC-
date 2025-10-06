package com.example.myapplication

import com.example.myapplication.model.Cartao
import com.example.myapplication.remote.CartaoApi

class CartaoRepository(private val cartaoApi: CartaoApi) {

    suspend fun adicionarCartao(novoCartao: Cartao): Result<Cartao> {
        return try {
            val response = cartaoApi.addCartao(novoCartao)
            if (response.isSuccessful) {
                // Sucesso! Retorna o corpo da resposta.
                Result.success(response.body()!!)
            } else {
                // Erro da API (ex: 404, 500)
                Result.failure(Exception("Erro na resposta da API: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Erro de rede ou outro (ex: sem conexão)
            Result.failure(e)
        }
    }
}