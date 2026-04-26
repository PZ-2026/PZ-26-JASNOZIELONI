package pl.edu.ur.coopspace.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pl.edu.ur.coopspace.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class BackendAuthResponse(
    val token: String,
    val id: Int?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val role: String?,
    val localId: Int?
)

object AuthApiClient {
    suspend fun login(email: String, password: String): Result<BackendAuthResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
            val url = URL("$baseUrl/api/auth/login")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val requestBody = JSONObject()
                .put("email", email)
                .put("password", password)
                .toString()

            connection.outputStream.use { output ->
                output.write(requestBody.toByteArray())
            }

            val responseCode = connection.responseCode
            val responseBody = readResponseBody(connection)

            if (responseCode !in 200..299) {
                val errorMessage = extractErrorMessage(responseBody)
                throw IllegalStateException(errorMessage)
            }

            val json = JSONObject(responseBody)
            BackendAuthResponse(
                token = json.optString("token", ""),
                id = json.optIntOrNull("id"),
                email = json.optStringOrNull("email"),
                firstName = json.optStringOrNull("firstName"),
                lastName = json.optStringOrNull("lastName"),
                phoneNumber = json.optStringOrNull("phoneNumber"),
                role = json.optStringOrNull("role"),
                localId = json.optIntOrNull("localId")
            )
        }
    }

    private fun readResponseBody(connection: HttpURLConnection): String {
        val stream = connection.errorStream ?: connection.inputStream
        return stream?.use {
            BufferedReader(InputStreamReader(it)).readText()
        }.orEmpty()
    }

    private fun extractErrorMessage(responseBody: String): String {
        if (responseBody.isBlank()) {
            return "Logowanie nie powiodło się"
        }

        return runCatching {
            val json = JSONObject(responseBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> "Logowanie nie powiodło się"
            }
        }.getOrElse { "Logowanie nie powiodło się" }
    }
}

private fun JSONObject.optIntOrNull(name: String): Int? {
    return if (isNull(name)) null else optInt(name)
}

private fun JSONObject.optStringOrNull(name: String): String? {
    val value = optString(name, "")
    return value.ifBlank { null }
}
