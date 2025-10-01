package com.example.myapplication.model

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id" // <-- Chave para o ID do usuário
    }

    // Salva tanto o token quanto o ID do usuário
    fun saveAuthData(token: String, userId: Int) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putInt(USER_ID, userId) // <-- Salva o ID
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // Função para buscar o ID do usuário
    fun fetchUserId(): Int? {
        // Retorna -1 ou null se não encontrar, para indicar erro.
        val userId = prefs.getInt(USER_ID, -1)
        return if (userId != -1) userId else null
    }

    fun clearAuthData() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
