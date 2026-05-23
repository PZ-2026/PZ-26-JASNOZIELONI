package pl.edu.ur.coopspace.maintainer_module

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import android.widget.Toast
import pl.edu.ur.coopspace.auth.AuthSessionStore
import pl.edu.ur.coopspace.ticket_module.IssueApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintainerFinishedReportsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onTicketClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var localFilterText by remember { mutableStateOf("") }
    var reports by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun fetchReports(localId: Int?) {
        isLoading = true
        errorMessage = null

        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj się ponownie."
            isLoading = false
            return
        }

        // Fetch ONLY assigned issues with status CLOSED
        IssueApiClient.getAssignedIssues(token, status = "CLOSED", localId = localId)
            .onSuccess { issues ->
                reports = issues.map { issue ->
                    issue.id to "${issue.title} | Lokal ${issue.localId ?: "-"}"
                }
            }
            .onFailure { throwable ->
                errorMessage = throwable.message ?: "Nie udało się pobrać zakończonych zgłoszeń"
            }

        isLoading = false
    }

    LaunchedEffect(Unit) {
        fetchReports(localId = null)
    }

    val filteredReports = reports.filter {
        searchQuery.isBlank() || it.second.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Nagłówek
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Moje Zakończone Zgłoszenia",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Wyloguj", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pole wyszukiwania
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Wyszukaj w zgłoszeniach", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Sort/Filter",
                    tint = Color.DarkGray
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Szukaj",
                    tint = Color.DarkGray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEBE6F3),
                unfocusedContainerColor = Color(0xFFEBE6F3),
                disabledContainerColor = Color(0xFFEBE6F3),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = localFilterText,
                onValueChange = { localFilterText = it.filter(Char::isDigit) },
                label = { Text("Lokal") },
                singleLine = true,
                modifier = Modifier.width(140.dp)
            )

            Button(
                onClick = {
                    val localParam = localFilterText.takeIf { it.isNotBlank() }?.toIntOrNull()
                    coroutineScope.launch {
                        fetchReports(localId = localParam)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Filtruj")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        } else if (filteredReports.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Brak zgłoszeń do wyświetlenia", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(filteredReports) { index, report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTicketClick(report.first) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF90D18F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = report.second,
                                fontSize = 15.sp,
                                color = Color.Black.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 16.dp)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        val token = AuthSessionStore.getToken(context)
                                        if (token.isNullOrBlank()) {
                                            Toast.makeText(context, "Brak sesji użytkownika", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        IssueApiClient.enqueueRepairProtocolDownload(context, token, report.first)
                                            .onSuccess {
                                                Toast.makeText(context, "Rozpoczęto pobieranie", Toast.LENGTH_SHORT).show()
                                            }
                                            .onFailure {
                                                Toast.makeText(context, "Nie udało się rozpocząć pobierania", Toast.LENGTH_SHORT).show()
                                            }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("Generuj", color = Color.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk Cofnij
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(text = "Cofnij", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
