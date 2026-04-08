package pl.edu.ur.coopspace.ticket_module

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.BuildConfig
import pl.edu.ur.coopspace.auth.AuthSessionStore

@Composable
fun IssueImagesGallery(
    issueId: Int,
    modifier: Modifier = Modifier,
    managementEnabled: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var images by remember { mutableStateOf<List<IssueImageDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isMutating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) {
            return@rememberLauncherForActivityResult
        }

        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            isMutating = true
            errorMessage = null

            for (uri in uris) {
                val uploadResult = IssueApiClient.uploadIssueImage(token, issueId, context, uri)
                if (uploadResult.isFailure) {
                    errorMessage = uploadResult.exceptionOrNull()?.message ?: "Nie udalo sie dodac zdjecia"
                    break
                }
            }

            isMutating = false
            refreshKey++
        }
    }

    LaunchedEffect(issueId, refreshKey) {
        isLoading = true
        errorMessage = null

        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            isLoading = false
            return@LaunchedEffect
        }

        IssueApiClient.getIssueImages(token, issueId)
            .onSuccess { images = it }
            .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udalo sie pobrac zdjec" }

        isLoading = false
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Zdjęcia zgłoszenia", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (managementEnabled) {
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isMutating
            ) {
                Icon(imageVector = Icons.Outlined.AddPhotoAlternate, contentDescription = "Dodaj zdjęcia")
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (isMutating) "Zapisywanie..." else "Dodaj zdjęcia")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            images.isEmpty() -> {
                Text(text = "Brak zdjęć dla tego zgłoszenia", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(images) { image ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box {
                                val authToken = AuthSessionStore.getToken(context)
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(BuildConfig.BASE_URL.trimEnd('/') + image.downloadUrl)
                                        .apply {
                                            if (!authToken.isNullOrBlank()) {
                                                addHeader("Authorization", "Bearer $authToken")
                                            }
                                        }
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Zdjęcie zgłoszenia",
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )

                                if (managementEnabled) {
                                    IconButton(
                                        onClick = {
                                            val token = AuthSessionStore.getToken(context)
                                            if (token.isNullOrBlank()) {
                                                errorMessage = "Brak sesji. Zaloguj sie ponownie."
                                                return@IconButton
                                            }

                                            coroutineScope.launch {
                                                isMutating = true
                                                errorMessage = null

                                                IssueApiClient.deleteIssueImage(token, issueId, image.id)
                                                    .onFailure { throwable ->
                                                        errorMessage = throwable.message ?: "Nie udalo sie usunac zdjecia"
                                                    }

                                                isMutating = false
                                                refreshKey++
                                            }
                                        },
                                        enabled = !isMutating,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń zdjęcie")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}