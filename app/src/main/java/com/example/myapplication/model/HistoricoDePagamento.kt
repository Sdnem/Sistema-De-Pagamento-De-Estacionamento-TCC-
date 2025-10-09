package com.example.myapplication.model

// 1. Modelo de Dados para cada item do histórico
data class HistoricoDePagamento(
    val id: String,
    val data: String,
    val placaVeiculo: String,
    val valor: String,
    val status: String
)