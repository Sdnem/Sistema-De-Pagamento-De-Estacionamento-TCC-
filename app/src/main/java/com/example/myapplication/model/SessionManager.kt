package com.example.myapplication.model

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREFS_NAME = "app_session"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"

    // Função para obter a instância do SharedPreferences
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ==========================================================
    // NOVA FUNÇÃO PARA SALVAR O TOKEN
    // ==========================================================
    fun saveAuthToken(context: Context, token: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    // ==========================================================
    // NOVA FUNÇÃO PARA RECUPERAR O TOKEN
    // ==========================================================
    fun getAuthToken(context: Context): String? {
        return getPreferences(context).getString(KEY_AUTH_TOKEN, null)
    }

    // Função para salvar os dados do usuário (exemplo)
    fun saveUserData(context: Context, userId: Int, userName: String) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.apply()
    }

    // Função para recuperar o ID do usuário
    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, -1) // Retorna -1 se não encontrar
    }

    // Função para recuperar o nome do usuário
    fun getUserName(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_NAME, null)
    }

    // Função para limpar a sessão (logout)
    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
