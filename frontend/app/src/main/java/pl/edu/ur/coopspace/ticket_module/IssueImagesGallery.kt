package pl.edu.ur.coopspace.ticket_module

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.auth.AuthSessionStore
import pl.edu.ur.coopspace.network.BackendUrlStore

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
    var selectedImage by remember { mutableStateOf<IssueImageDto?>(null) }
    var imageToDelete by remember { mutableStateOf<IssueImageDto?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) {
            return@rememberLauncherForActivityResult
        }

        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj się ponownie."
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            isMutating = true
            errorMessage = null

            for (uri in uris) {
                val uploadResult = IssueApiClient.uploadIssueImage(token, issueId, context, uri)
                if (uploadResult.isFailure) {
                    errorMessage = uploadResult.exceptionOrNull()?.message ?: "Nie udało się dodać zdjęcia"
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
            errorMessage = "Brak sesji. Zaloguj się ponownie."
            isLoading = false
            return@LaunchedEffect
        }

        IssueApiClient.getIssueImages(token, issueId)
            .onSuccess { images = it }
            .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udało się pobrać zdjęć" }

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
                val itemSize = 140.dp

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    images.forEachIndexed { index, image ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.size(itemSize)
                        ) {
                            Box {
                                val authToken = AuthSessionStore.getToken(context)
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(BackendUrlStore.getBaseUrl().trimEnd('/') + image.downloadUrl)
                                        .apply {
                                            if (!authToken.isNullOrBlank()) {
                                                addHeader("Authorization", "Bearer $authToken")
                                            }
                                        }
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Zdjęcie zgłoszenia",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedImage = image }
                                )

                                Text(
                                    text = "${index + 1}/${images.size}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                if (managementEnabled) {
                                    IconButton(
                                        onClick = { imageToDelete = image },
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

    if (selectedImage != null) {
        var zoom by remember(selectedImage) { mutableStateOf(1f) }
        var offsetX by remember(selectedImage) { mutableStateOf(0f) }
        var offsetY by remember(selectedImage) { mutableStateOf(0f) }
        val transformState = androidx.compose.foundation.gestures.rememberTransformableState { zoomChange, offsetChange, _ ->
            zoom = (zoom * zoomChange).coerceIn(1f, 4f)
            offsetX += offsetChange.x
            offsetY += offsetChange.y
        }

        Dialog(
            onDismissRequest = { selectedImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val authToken = AuthSessionStore.getToken(context)
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(BackendUrlStore.getBaseUrl().trimEnd('/') + selectedImage!!.downloadUrl)
                        .apply {
                            if (!authToken.isNullOrBlank()) {
                                addHeader("Authorization", "Bearer $authToken")
                            }
                        }
                        .crossfade(true)
                        .build(),
                    contentDescription = "Podgląd zdjęcia",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .transformable(transformState)
                        .graphicsLayer(
                            scaleX = zoom,
                            scaleY = zoom,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )

                IconButton(
                    onClick = { selectedImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Zamknij")
                }
            }
        }
    }

    if (imageToDelete != null) {
        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text(text = "Usunąć zdjęcie?") },
            text = { Text(text = "Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val token = AuthSessionStore.getToken(context)
                        if (token.isNullOrBlank()) {
                            errorMessage = "Brak sesji. Zaloguj się ponownie."
                            imageToDelete = null
                            return@TextButton
                        }

                        coroutineScope.launch {
                            isMutating = true
                            errorMessage = null

                            IssueApiClient.deleteIssueImage(token, issueId, imageToDelete!!.id)
                                .onFailure { throwable ->
                                    errorMessage = throwable.message ?: "Nie udało się usunąć zdjęcia"
                                }

                            isMutating = false
                            refreshKey++
                            imageToDelete = null
                        }
                    }
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { imageToDelete = null }) {
                    Text("Anuluj")
                }
            }
        )
    }
}