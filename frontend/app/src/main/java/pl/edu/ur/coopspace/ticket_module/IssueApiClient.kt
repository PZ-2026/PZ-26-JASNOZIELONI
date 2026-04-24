package pl.edu.ur.coopspace.ticket_module

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.ur.coopspace.BuildConfig
import java.io.BufferedOutputStream
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

data class IssueImageDto(
    val id: Int,
    val issueId: Int,
    val filePath: String,
    val downloadUrl: String,
    val createdAt: String?
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

    suspend fun getIssueImages(token: String, issueId: Int): Result<List<IssueImageDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/issues/$issueId/images", token)
            val array = JSONArray(response)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        IssueImageDto(
                            id = item.getInt("id"),
                            issueId = item.getInt("issueId"),
                            filePath = item.optString("filePath", ""),
                            downloadUrl = item.optString("downloadUrl", ""),
                            createdAt = item.optStringOrNull("createdAt")
                        )
                    )
                }
            }
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

    suspend fun uploadIssueImage(
        token: String,
        issueId: Int,
        context: Context,
        uri: Uri
    ): Result<IssueImageDto> = withContext(Dispatchers.IO) {
        runCatching {
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val fileName = resolveFileName(context, uri, mimeType)
            val fileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Nie udało się odczytać zdjęcia")

            val boundary = "----CoopSpaceBoundary${System.currentTimeMillis()}"
            val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
            val url = URL("$baseUrl/api/issues/$issueId/images")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                doOutput = true
            }

            connection.outputStream.use { rawOutput ->
                val output = BufferedOutputStream(rawOutput)
                writeMultipartFile(output, boundary, "file", fileName, mimeType, fileBytes)
                output.write("--$boundary--\r\n".toByteArray())
                output.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = readResponseBody(connection)
            if (responseCode !in 200..299) {
                throw IllegalStateException(extractErrorMessage(responseBody))
            }

            parseImage(JSONObject(responseBody))
        }
    }

    suspend fun deleteIssueImage(token: String, issueId: Int, imageId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            request("DELETE", "/api/issues/$issueId/images/$imageId", token)
            Unit
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

    private fun parseImage(json: JSONObject): IssueImageDto {
        return IssueImageDto(
            id = json.getInt("id"),
            issueId = json.getInt("issueId"),
            filePath = json.optString("filePath", ""),
            downloadUrl = json.optString("downloadUrl", ""),
            createdAt = json.optStringOrNull("createdAt")
        )
    }

    private fun writeMultipartFile(
        output: BufferedOutputStream,
        boundary: String,
        fieldName: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ) {
        output.write("--$boundary\r\n".toByteArray())
        output.write("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"\r\n".toByteArray())
        output.write("Content-Type: $mimeType\r\n\r\n".toByteArray())
        output.write(bytes)
        output.write("\r\n".toByteArray())
    }

    private fun resolveFileName(context: Context, uri: Uri, mimeType: String): String {
        val extension = when {
            mimeType.contains("png", ignoreCase = true) -> ".png"
            mimeType.contains("webp", ignoreCase = true) -> ".webp"
            mimeType.contains("jpg", ignoreCase = true) || mimeType.contains("jpeg", ignoreCase = true) -> ".jpg"
            else -> ".jpg"
        }

        val lastSegment = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        return if (lastSegment.isNullOrBlank()) {
            "issue_image$extension"
        } else {
            lastSegment
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
            return "Operacja nie powiodła się"
        }

        return runCatching {
            val json = JSONObject(responseBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> "Operacja nie powiodła się"
            }
        }.getOrElse { "Operacja nie powiodła się" }
    }
}

private fun JSONObject.optIntOrNull(name: String): Int? {
    return if (isNull(name)) null else optInt(name)
}

private fun JSONObject.optStringOrNull(name: String): String? {
    if (!has(name) || isNull(name)) {
        return null
    }

    val value = optString(name, "")
    return value.ifBlank { null }
}
