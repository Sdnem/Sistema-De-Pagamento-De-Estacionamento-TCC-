package com.example.myapplication.model

data class Carro(
    val id: Int? = null,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val placa: String,
    val cor: String,
    val imageUrl: String, // URL da imagem do carro
    val userId: Int // Chave estrangeira para associar ao usuário
)