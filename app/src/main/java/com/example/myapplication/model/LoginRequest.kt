package com.example.myapplication.model

// Esta é a classe de dados que o ApiService estava procurando.
data class LoginRequest(
    val email: String,
    val senha_hash: String // Certifique-se que o nome corresponde exatamente ao que sua API espera no JSON.
)
