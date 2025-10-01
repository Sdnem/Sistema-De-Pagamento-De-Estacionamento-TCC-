package com.example.myapplication.model

data class Cartao(
    val id: Int? = null, // O ID pode ser nulo ao criar, o backend irá gerá-lo
    val banco: String,
    val nome: String,
    val numero: Long,
    val dataValidade: String,
    val cvc: Int,
    val userId: Int // Chave estrangeira para associar ao usuário
)