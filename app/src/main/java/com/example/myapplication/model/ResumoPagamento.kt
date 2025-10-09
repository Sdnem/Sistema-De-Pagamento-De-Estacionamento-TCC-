package com.example.myapplication.model

data class ResumoPagamentoData(
    val placaVeiculo: String,
    val modeloVeiculo: String,
    val entrada: String,
    val saida: String,
    val tempoTotal: String,
    val valorTotal: String,
    val metodoPagamento: String,
    val finalCartao: String
)