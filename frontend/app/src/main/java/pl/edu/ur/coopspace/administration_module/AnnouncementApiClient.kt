package pl.edu.ur.coopspace.administration_module

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pl.edu.ur.coopspace.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AnnouncementDto(
    val id: Int,
    val title: String,
    val content: String,
    val createdBy: Int,
    val createdAt: String
)

data class DocumentDto(
    val id: Int,
    val title: String,
    val filePath: String,
    val uploadedBy: Int,
    val createdAt: String
)

object AnnouncementApiClient {
    suspend fun createAnnouncement(
        token: String,
        title: String,
        content: String
    ): Result<AnnouncementDto> = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject()
                .put("title", title)
                .put("content", content)
                .toString()

            val response = request("POST", "/api/announcements", token, body)
            parseAnnouncement(JSONObject(response))
        }
    }

    suspend fun getAnnouncements(token: String): Result<List<AnnouncementDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/announcements", token)
            val array = JSONArray(response)
            
            buildList {
                for (index in 0 until array.length()) {
                    add(parseAnnouncement(array.getJSONObject(index)))
                }
            }
        }
    }

    suspend fun getAnnouncementById(token: String, id: Int): Result<AnnouncementDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/announcements/$id", token)
            parseAnnouncement(JSONObject(response))
        }
    }

    suspend fun deleteAnnouncements(token: String, ids: List<Int>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (ids.isEmpty()) return@runCatching
            val idsParam = ids.joinToString(",")
            request("DELETE", "/api/announcements?ids=$idsParam", token)
        }
    }

    suspend fun getDocuments(token: String): Result<List<DocumentDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = request("GET", "/api/announcements/documents", token)
            val array = JSONArray(response)
            
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    add(
                        DocumentDto(
                            id = json.getInt("id"),
                            title = json.optString("title", ""),
                            filePath = json.optString("filePath", ""),
                            uploadedBy = json.optInt("uploadedBy"),
                            createdAt = json.optString("createdAt", "")
                        )
                    )
                }
            }
        }
    }

    suspend fun uploadDocument(
        token: String,
        context: Context,
        uri: Uri
    ): Result<DocumentDto> = withContext(Dispatchers.IO) {
        runCatching {
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val fileName = resolveFileName(context, uri, mimeType)
            val fileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Nie udało się odczytać pliku")

            val boundary = "----CoopSpaceBoundary${System.currentTimeMillis()}"
            val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
            val url = URL("$baseUrl/api/announcements/documents")
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
                val output = java.io.BufferedOutputStream(rawOutput)
                output.write("--$boundary\r\n".toByteArray())
                output.write("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n".toByteArray())
                output.write("Content-Type: $mimeType\r\n\r\n".toByteArray())
                output.write(fileBytes)
                output.write("\r\n".toByteArray())
                output.write("--$boundary--\r\n".toByteArray())
                output.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = readResponseBody(connection)
            if (responseCode !in 200..299) {
                throw IllegalStateException(extractErrorMessage(responseBody))
            }

            val json = JSONObject(responseBody)
            DocumentDto(
                id = json.getInt("id"),
                title = json.optString("title", ""),
                filePath = json.optString("filePath", ""),
                uploadedBy = json.optInt("uploadedBy"),
                createdAt = json.optString("createdAt", "")
            )
        }
    }

    suspend fun deleteDocument(token: String, id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            request("DELETE", "/api/announcements/documents/$id", token)
            Unit
        }
    }

    fun enqueueDocumentDownload(context: Context, token: String, document: DocumentDto): Result<Unit> = runCatching {
        val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
        val url = "$baseUrl/api/announcements/documents/${document.id}/download"

        val sanitizedTitle = document.title.ifBlank { "dokument-${document.id}" }
            .replace('/', '_')
            .replace('\\', '_')
        val request = DownloadManager.Request(Uri.parse(url))
            .setMimeType("application/octet-stream")
            .setTitle(sanitizedTitle)
            .setDescription("Pobieranie dokumentu")
            .addRequestHeader("Authorization", "Bearer $token")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, sanitizedTitle)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    private fun resolveFileName(context: Context, uri: Uri, mimeType: String): String {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = cursor.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.lastPathSegment?.substringAfterLast('/')
        }

        if (name.isNullOrBlank()) {
            val extension = when {
                mimeType.contains("pdf", ignoreCase = true) -> ".pdf"
                mimeType.contains("word", ignoreCase = true) -> ".docx"
                mimeType.contains("png", ignoreCase = true) -> ".png"
                mimeType.contains("jpg", ignoreCase = true) || mimeType.contains("jpeg", ignoreCase = true) -> ".jpg"
                else -> ".file"
            }
            name = "document$extension"
        }
        return name!!
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

    private fun parseAnnouncement(json: JSONObject): AnnouncementDto {
        return AnnouncementDto(
            id = json.getInt("id"),
            title = json.optString("title", ""),
            content = json.optString("content", ""),
            createdBy = json.optInt("createdBy"),
            createdAt = json.optString("createdAt", "")
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
