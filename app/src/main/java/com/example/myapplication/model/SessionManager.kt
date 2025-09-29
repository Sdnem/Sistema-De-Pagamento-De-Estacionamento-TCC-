package com.example.myapplication.model

import android.content.Context
import android.content.SharedPreferences

// Classe para gerenciar o token do usuário
class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val AUTH_TOKEN = "auth_token"
    }

    // Salva o token após o login
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(AUTH_TOKEN, token)
        editor.apply()
    }

    // Busca o token para usar nas chamadas de API
    fun fetchAuthToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }
}