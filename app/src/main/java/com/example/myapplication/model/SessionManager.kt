package com.example.myapplication.model

import android.content.Context
import android.content.SharedPreferences

interface SessionManager {
    fun saveAuthToken(token: String)
    fun fetchAuthToken(): String?
}

// Classe para gerenciar o token do usuário
class SessionManagerImpl(context: Context) : SessionManager {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val AUTH_TOKEN = "auth_token"
    }

    // A anotação @Override é adicionada para indicar que vem da interface
    override fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(AUTH_TOKEN, token)
        editor.apply()
    }

    override fun fetchAuthToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }
}