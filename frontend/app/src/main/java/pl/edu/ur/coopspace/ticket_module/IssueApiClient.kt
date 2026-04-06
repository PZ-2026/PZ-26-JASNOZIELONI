package pl.edu.ur.coopspace.ticket_module

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.ur.coopspace.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class IssueDto(
    val id: Int,
    val title: String,
    val description: String,
    val categoryId: Int?,
    val localId: Int?,
    val status: String
)

data class IssueCategoryDto(
    val id: Int,
    val name: String
)

data class MaintainerDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String
)

object IssueApiClient {
    suspend fun getAllIssues(token: String, status: String? = null, localId: Int? = null): Result<List<IssueDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val queryParts = mutableListOf<String>()
            if (!status.isNullOrBlank()) {
                queryParts.add("status=$status")
            }
            if (localId != null) {
                queryParts.add("localId=$localId")
            }

            val query = if (queryParts.isEmpty()) "" else "?${queryParts.joinToString("&")}" 
            val response = request("GET", "/api/issues$query", token)
            parseIssues(response)
        }
    }

    suspend fun getMyIssues(token: String): Result<List<IssueDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/issues/my", token)
            parseIssues(response)
        }
    }

    suspend fun getAssignedIssues(token: String): Result<List<IssueDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/issues/assigned", token)
            parseIssues(response)
        }
    }

    suspend fun getIssueCategories(token: String): Result<List<IssueCategoryDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/issues/categories", token)
            val array = JSONArray(response)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(IssueCategoryDto(id = item.getInt("id"), name = item.getString("name")))
                }
            }
        }
    }

    suspend fun createIssue(token: String, title: String, description: String, categoryId: Int): Result<IssueDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = JSONObject()
                    .put("title", title)
                    .put("description", description)
                    .put("categoryId", categoryId)
                    .toString()

                val response = request("POST", "/api/issues", token, body)
                parseIssue(JSONObject(response))
            }
        }

    suspend fun updateIssueStatus(token: String, issueId: Int, status: String): Result<IssueDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = JSONObject()
                    .put("status", status)
                    .toString()

                val response = request("PATCH", "/api/issues/$issueId/status", token, body)
                parseIssue(JSONObject(response))
            }
        }

    suspend fun assignIssue(token: String, issueId: Int, assigneeUserId: Int): Result<IssueDto> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = JSONObject()
                    .put("assigneeUserId", assigneeUserId)
                    .toString()

                val response = request("PATCH", "/api/issues/$issueId/assignee", token, body)
                parseIssue(JSONObject(response))
            }
        }

    suspend fun getMaintainers(token: String): Result<List<MaintainerDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/users/maintainers", token)
            val array = JSONArray(response)

            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        MaintainerDto(
                            id = item.getInt("id"),
                            firstName = item.optString("firstName", ""),
                            lastName = item.optString("lastName", ""),
                            email = item.optString("email", "")
                        )
                    )
                }
            }
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

    private fun parseIssues(json: String): List<IssueDto> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                add(parseIssue(array.getJSONObject(index)))
            }
        }
    }

    private fun parseIssue(json: JSONObject): IssueDto {
        return IssueDto(
            id = json.getInt("id"),
            title = json.optString("title", ""),
            description = json.optString("description", ""),
            categoryId = json.optIntOrNull("categoryId"),
            localId = json.optIntOrNull("localId"),
            status = json.optString("status", "OPEN")
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
