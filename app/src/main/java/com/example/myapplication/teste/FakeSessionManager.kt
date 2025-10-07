package com.example.myapplication.teste

import com.example.myapplication.model.SessionManager

class FakeSessionManager : SessionManager {
    private var token: String? = "fake_token_for_preview"

    override fun saveAuthToken(token: String) {
        // No fake, podemos simular o salvamento ou simplesmente não fazer nada
        this.token = token
    }

    override fun fetchAuthToken(): String? {
        // Retorna um valor fixo para o Preview
        return this.token
    }
}