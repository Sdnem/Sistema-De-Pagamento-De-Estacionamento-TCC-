package com.example.myapplication.model

import java.time.LocalDateTime

data class ResumoPagamentoData(
    val sessaoId: Int,
    val cartaoId: Int,
    val entrada: String,
    val saida: String,
    val tempoTotal: String,
    val valorTotal: String,
    val finalCartao: String
)

data class DadosSessao(
    val entrada: LocalDateTime?,
    val saida: LocalDateTime?,
    val tempoTotal: String,
    val valorTotal: String
)