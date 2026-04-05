package pl.edu.ur.coopspace.auth

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

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

    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
    }

    fun getRole(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null)
    }

    fun isTokenValid(token: String): Boolean {
        return runCatching {
            val jwtParts = token.split('.')
            if (jwtParts.size != 3) {
                return false
            }

            val payloadBytes = Base64.decode(jwtParts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payload = String(payloadBytes, StandardCharsets.UTF_8)
            val payloadJson = JSONObject(payload)

            if (!payloadJson.has("exp")) {
                return false
            }

            val expEpochSeconds = payloadJson.getLong("exp")
            val currentEpochSeconds = System.currentTimeMillis() / 1000
            expEpochSeconds > currentEpochSeconds
        }.getOrDefault(false)
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
