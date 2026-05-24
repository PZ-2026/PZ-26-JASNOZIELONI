package pl.edu.ur.coopspace.ticket_module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.edu.ur.coopspace.auth.AuthSessionStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentTicketDetailsScreen(
    ticketId: String,
    onBackClick: () -> Unit
) {
    var issue by remember { mutableStateOf<IssueDto?>(null) }
    var categoryNameById by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val parsedTicketId = ticketId.toIntOrNull()

    LaunchedEffect(ticketId) {
        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj się ponownie."
            isLoading = false
            return@LaunchedEffect
        }

        if (parsedTicketId == null) {
            errorMessage = "Nieprawidłowe id zgłoszenia"
            isLoading = false
            return@LaunchedEffect
        }

        IssueApiClient.getIssueCategories(token)
            .onSuccess { categories ->
                categoryNameById = categories.associate { category -> category.id to category.name }
            }
            .onFailure { throwable ->
                errorMessage = throwable.message ?: "Nie udało się pobrać kategorii"
            }

        IssueApiClient.getMyIssues(token)
            .onSuccess { issues ->
                issue = issues.firstOrNull { it.id == parsedTicketId }
                if (issue == null) {
                    errorMessage = "Nie znaleziono zgłoszenia"
                }
            }
            .onFailure { throwable ->
                errorMessage = throwable.message ?: "Nie udało się pobrać szczegółów"
            }

        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Zgłoszenie $ticketId",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            modifier = Modifier.padding(4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tytuł zgłoszenia
                    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                        Text(
                            text = "Tytuł zgłoszenia",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = issue?.title ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Opis zgłoszenia
                    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                        Text(
                            text = "Opis zgłoszenia",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = issue?.description ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kategoria zgłoszenia
                    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                        Text(
                            text = "Kategoria zgłoszenia",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = issue?.categoryId?.let { categoryId ->
                                categoryNameById[categoryId] ?: "Kategoria #$categoryId"
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status zgłoszenia
                    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                        Text(
                            text = "Status zgłoszenia",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = issue?.status?.toStatusLabel() ?: "",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    if (issue != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        IssueImagesGallery(
                            issueId = issue!!.id,
                            managementEnabled = false // Resident mołby tutaj tylko przeglądać lub ew. zarządzać w panelu edycji
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        pl.edu.ur.coopspace.user_module.UserBackButton(onBack = onBackClick)
    }
}

private fun String.toStatusLabel(): String {
    return when (this.uppercase()) {
        "OPEN" -> "Nowe"
        "IN_PROGRESS" -> "W trakcie"
        "CLOSED" -> "Zamkniete"
        else -> this
    }
}
