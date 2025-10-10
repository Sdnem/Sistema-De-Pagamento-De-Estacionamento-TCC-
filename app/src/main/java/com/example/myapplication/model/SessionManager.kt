package com.example.myapplication.model

import android.content.Context
import android.content.SharedPreferences

/**
 * Objeto Singleton para gerenciar a sessão do usuário usando SharedPreferences.
 * Sendo um 'object', só existe uma instância dele em todo o app e suas funções
 * podem ser chamadas diretamente (Ex: SessionManager.getUserId(...)).
 */
object SessionManager {

    private const val PREFS_NAME = "MyAppSession"
    private const val USER_ID = "user_id"
    // NOVA CONSTANTE para salvar o nome do usuário
    private const val USER_NAME = "user_name"

    // Função auxiliar para não repetir código
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Salva os dados do usuário após o login.
     * MUDANÇA: Agora salva o ID e o NOME.
     */
    fun saveUserData(context: Context, userId: Int, userName: String?) {
        val editor = getPrefs(context).edit()
        editor.putInt(USER_ID, userId)
        editor.putString(USER_NAME, userName) // Salva o nome
        editor.apply()
    }

    /**
     * Busca o ID do usuário salvo.
     * Retorna -1 se não houver ID salvo.
     */
    fun getUserId(context: Context): Int {
        return getPrefs(context).getInt(USER_ID, -1)
    }

    /**
     * NOVA FUNÇÃO para buscar o nome do usuário salvo.
     * Retorna null se não houver nome salvo.
     * É esta função que a HomeScreen vai chamar.
     */
    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(USER_NAME, null)
    }

    /**
     * Limpa todos os dados da sessão (usado para logout).
     */
    fun clearSession(context: Context) {
        val editor = getPrefs(context).edit()
        editor.clear()
        editor.apply()
    }
}
