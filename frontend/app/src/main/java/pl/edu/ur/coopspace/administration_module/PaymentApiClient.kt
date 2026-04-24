package pl.edu.ur.coopspace.administration_module

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pl.edu.ur.coopspace.BuildConfig
import pl.edu.ur.coopspace.auth.AuthSessionStore
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class CurrentRates(
    val rentRate: String,
    val waterRate: String,
    val electricityRate: String,
    val gasRate: String
)

object PaymentApiClient {
    suspend fun getRates(context: Context): Result<CurrentRates> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
            val url = URL("$baseUrl/api/admin/payments/rates")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Accept", "application/json")
                val token = AuthSessionStore.getToken(context)
                if (!token.isNullOrEmpty()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }

            val responseCode = connection.responseCode
            val responseBody = readResponseBody(connection)

            if (responseCode !in 200..299) {
                throw IllegalStateException("Nie udało się pobrać stawek. Kod: $responseCode")
            }

            val json = JSONObject(responseBody)
            CurrentRates(
                rentRate = json.optString("rentRate", "0"),
                waterRate = json.optString("waterRate", "0"),
                electricityRate = json.optString("electricityRate", "0"),
                gasRate = json.optString("gasRate", "0")
            )
        }
    }

    suspend fun updateRates(
        context: Context,
        rentRate: String?,
        waterRate: String?,
        electricityRate: String?,
        gasRate: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
            val url = URL("$baseUrl/api/admin/payments/rates")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                // Add Authorization header assuming the user is logged in
                val token = AuthSessionStore.getToken(context)
                if (!token.isNullOrEmpty()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }

            val requestBody = JSONObject().apply {
                if (!rentRate.isNullOrBlank()) put("rentRate", rentRate.toDoubleOrNull())
                if (!waterRate.isNullOrBlank()) put("waterRate", waterRate.toDoubleOrNull())
                if (!electricityRate.isNullOrBlank()) put("electricityRate", electricityRate.toDoubleOrNull())
                if (!gasRate.isNullOrBlank()) put("gasRate", gasRate.toDoubleOrNull())
            }.toString()

            connection.outputStream.use { output ->
                output.write(requestBody.toByteArray())
            }

            val responseCode = connection.responseCode
            val responseBody = readResponseBody(connection)

            if (responseCode !in 200..299) {
                throw IllegalStateException("Aktualizacja stawek nie powiodła się. Kod serwera: $responseCode")
            }
        }
    }

    private fun readResponseBody(connection: HttpURLConnection): String {
        val stream = connection.errorStream ?: connection.inputStream
        return stream?.use {
            BufferedReader(InputStreamReader(it)).readText()
        }.orEmpty()
    }
}
