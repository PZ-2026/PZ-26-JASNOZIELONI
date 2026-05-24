package pl.edu.ur.coopspace.maintainer_module

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.administration_module.AnnouncementApiClient
import pl.edu.ur.coopspace.administration_module.DocumentDto
import pl.edu.ur.coopspace.auth.AuthSessionStore

@Composable
fun MaintainerDocumentsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val token = remember { AuthSessionStore.getToken(context) }
    val scope = rememberCoroutineScope()
    
    var documents by remember { mutableStateOf<List<DocumentDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshDocuments() {
        if (token != null) {
            scope.launch {
                isLoading = true
                AnnouncementApiClient.getDocuments(token)
                    .onSuccess { data ->
                        documents = data
                        isLoading = false
                    }
                    .onFailure {
                        isLoading = false
                        Toast.makeText(context, "Błąd pobierania dokumentów", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(token) {
        refreshDocuments()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dokumenty",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Wyloguj",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Lista dokumentów
        if (isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (documents.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Brak dokumentów", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(documents.size) { index ->
                    val doc = documents[index]
                    val documentType = resolveDocumentType(doc)
                    val typeColor = documentTypeColor(documentType)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ikona pliku
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = "Plik",
                            tint = typeColor,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Nazwa pliku i dane
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val dateString = doc.createdAt.substringBefore("T")
                            Text(
                                text = "Typ: $documentType • Dodano: $dateString",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Przycisk Pobierz
                        IconButton(onClick = {
                            if (token == null) {
                                Toast.makeText(context, "Brak sesji użytkownika", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }

                            AnnouncementApiClient.enqueueDocumentDownload(context, token, doc)
                                .onSuccess {
                                    Toast.makeText(context, "Rozpoczęto pobieranie", Toast.LENGTH_SHORT).show()
                                }
                                .onFailure {
                                    Toast.makeText(context, "Nie udało się rozpocząć pobierania", Toast.LENGTH_SHORT).show()
                                }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Pobierz",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Przycisk Cofnij (Do prawej)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(text = "Cofnij", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun resolveDocumentType(document: DocumentDto): String {
    val extension = extractExtension(document.title)
        ?: extractExtension(document.filePath)
        ?: return "PLIK"

    return when (extension) {
        "pdf" -> "PDF"
        "doc", "docx", "odt" -> "DOKUMENT"
        "xls", "xlsx", "ods", "csv" -> "ARKUSZ"
        "png", "jpg", "jpeg", "gif", "webp", "bmp" -> "OBRAZ"
        "txt", "rtf", "md" -> "TEKST"
        else -> extension.uppercase()
    }
}

private fun extractExtension(fileName: String?): String? {
    if (fileName.isNullOrBlank()) {
        return null
    }

    val dotIndex = fileName.lastIndexOf('.')
    if (dotIndex < 0 || dotIndex == fileName.length - 1) {
        return null
    }

    return fileName.substring(dotIndex + 1).lowercase()
}

private fun documentTypeColor(documentType: String): Color {
    return when (documentType) {
        "PDF" -> Color(0xFFC62828)
        "OBRAZ" -> Color(0xFF2E7D32)
        "ARKUSZ" -> Color(0xFF1565C0)
        "DOKUMENT" -> Color(0xFF6A1B9A)
        "TEKST" -> Color(0xFF455A64)
        else -> Color.Gray
    }
}
