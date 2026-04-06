package pl.edu.ur.coopspace.administration_module

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.ur.coopspace.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AdminUserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String?,
    val role: String,
    val localId: Int?,
    val isActive: Boolean
)

object UserAdminApiClient {
    suspend fun getResidents(token: String): Result<List<AdminUserDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/users/residents", token)
            parseUsers(response)
        }
    }

    suspend fun getMaintainers(token: String): Result<List<AdminUserDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/users/maintainers", token)
            parseUsers(response)
        }
    }

    suspend fun createResident(
        token: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?,
        localId: Int
    ): Result<AdminUserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject()
                .put("email", email)
                .put("password", password)
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("phoneNumber", phoneNumber)
                .put("role", "RESIDENT")
                .put("localId", localId)
                .toString()

            val response = request("POST", "/api/users", token, body)
            parseUser(JSONObject(response))
        }
    }

    suspend fun createMaintainer(
        token: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?
    ): Result<AdminUserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject()
                .put("email", email)
                .put("password", password)
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("phoneNumber", phoneNumber)
                .put("role", "MAINTAINER")
                .toString()

            val response = request("POST", "/api/users", token, body)
            parseUser(JSONObject(response))
        }
    }

    suspend fun updateUserActiveState(
        token: String,
        userId: Int,
        isActive: Boolean
    ): Result<AdminUserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject()
                .put("isActive", isActive)
                .toString()

            val response = request("PATCH", "/api/users/$userId/active", token, body)
            parseUser(JSONObject(response))
        }
    }

    private fun request(method: String, path: String, token: String, body: String? = null): String {
        val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
        val url = URL("$baseUrl$path")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")

            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }

        if (body != null) {
            connection.outputStream.use { output ->
                output.write(body.toByteArray())
            }
        }

        val responseCode = connection.responseCode
        val responseBody = readResponseBody(connection)
        if (responseCode !in 200..299) {
            throw IllegalStateException(extractErrorMessage(responseBody))
        }

        return responseBody
    }

    private fun parseUsers(json: String): List<AdminUserDto> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                add(parseUser(array.getJSONObject(index)))
            }
        }
    }

    private fun parseUser(json: JSONObject): AdminUserDto {
        return AdminUserDto(
            id = json.getInt("id"),
            firstName = json.optString("firstName", ""),
            lastName = json.optString("lastName", ""),
            email = json.optString("email", ""),
            phoneNumber = json.optStringOrNull("phoneNumber"),
            role = json.optString("role", ""),
            localId = json.optIntOrNull("localId"),
            isActive = json.optBoolean("isActive", true)
        )
    }

    private fun readResponseBody(connection: HttpURLConnection): String {
        val stream = connection.errorStream ?: connection.inputStream
        return stream?.use {
            BufferedReader(InputStreamReader(it)).readText()
        }.orEmpty()
    }

    private fun extractErrorMessage(responseBody: String): String {
        if (responseBody.isBlank()) {
            return "Operacja nie powiodla sie"
        }

        return runCatching {
            val json = JSONObject(responseBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> "Operacja nie powiodla sie"
            }
        }.getOrElse { "Operacja nie powiodla sie" }
    }
}

private fun JSONObject.optIntOrNull(name: String): Int? {
    return if (isNull(name)) null else optInt(name)
}

private fun JSONObject.optStringOrNull(name: String): String? {
    val value = optString(name, "")
    return value.ifBlank { null }
}
