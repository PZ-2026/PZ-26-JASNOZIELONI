package pl.edu.ur.coopspace.auth

import android.content.Context

object AuthSessionStore {
    private const val PREFS_NAME = "coopspace_auth"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_ROLE = "user_role"

    fun saveSession(context: Context, token: String, email: String?, role: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
