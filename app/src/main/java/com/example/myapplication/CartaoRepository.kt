package com.example.myapplication

import com.example.myapplication.model.Cartao
import com.example.myapplication.remote.CartaoApi
import retrofit2.HttpException
import java.io.IOException

class CartaoRepository(private val cartaoApi: CartaoApi) {

    suspend fun adicionarCartao(novoCartao: Cartao): Result<Cartao> {
        return try {
            // Se a chamada for bem-sucedida (código 2xx), o objeto 'Cartao' será retornado diretamente.
            val cartaoAdicionado = cartaoApi.addCartao(novoCartao) //
            Result.success(cartaoAdicionado) //
        } catch (e: HttpException) {
            // Captura erros de API (ex: 404, 500)
            // O código do erro está dentro da exceção.
            Result.failure(Exception("Erro na resposta da API: ${e.code()}"))
        } catch (e: IOException) {
            // Captura erros de rede (ex: sem conexão)
            Result.failure(Exception("Erro de rede. Verifique sua conexão.", e))
        } catch (e: Exception) {
            // Captura quaisquer outros erros inesperados
            Result.failure(e)
        }
    }
}