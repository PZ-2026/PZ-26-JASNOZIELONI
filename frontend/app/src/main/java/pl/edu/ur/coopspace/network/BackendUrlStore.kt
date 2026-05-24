package pl.edu.ur.coopspace.network

import android.content.Context
import pl.edu.ur.coopspace.BuildConfig

object BackendUrlStore {
    private const val PREFS_NAME = "coopspace_backend"
    private const val KEY_BASE_URL = "base_url"

    @Volatile
    private var cachedBaseUrl: String? = null

    fun initialize(context: Context) {
        cachedBaseUrl = getPreferences(context).getString(KEY_BASE_URL, null)
            ?.takeIf { it.isNotBlank() }
            ?: BuildConfig.BASE_URL.trimEnd('/')
    }

    fun getBaseUrl(): String {
        return cachedBaseUrl ?: BuildConfig.BASE_URL.trimEnd('/')
    }

    fun setBaseUrl(context: Context, baseUrl: String) {
        val normalizedBaseUrl = normalize(baseUrl)
        cachedBaseUrl = normalizedBaseUrl
        getPreferences(context)
            .edit()
            .putString(KEY_BASE_URL, normalizedBaseUrl)
            .apply()
    }

    fun resetToDefault(context: Context) {
        cachedBaseUrl = BuildConfig.BASE_URL.trimEnd('/')
        getPreferences(context)
            .edit()
            .remove(KEY_BASE_URL)
            .apply()
    }

    private fun normalize(value: String): String {
        val trimmed = value.trim().trimEnd('/')
        if (trimmed.isBlank()) {
            return BuildConfig.BASE_URL.trimEnd('/')
        }

        return if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed
        } else {
            "http://$trimmed"
        }
    }

    private fun getPreferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}