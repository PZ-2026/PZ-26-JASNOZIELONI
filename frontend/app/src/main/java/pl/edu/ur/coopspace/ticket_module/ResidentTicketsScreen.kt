package pl.edu.ur.coopspace.ticket_module

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.auth.AuthSessionStore

// 1. Pseudo-model danych dla zgłoszenia
data class Ticket(
    val id: Int,
    val title: String,
    val description: String,
    val status: String // To posłuży jako "Overline" z makiety
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentTicketsScreen(
    ticketType: String = "CURRENT", // "CURRENT" or "FINISHED"
    onTicketClick: (Int) -> Unit, // Akcja po kliknięciu w konkretne zgłoszenie
    onBack: () -> Unit, // Akcja powrotu do poprzedniego ekranu
    onLogout: () -> Unit // Dodajemy akcję wylogowania
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun fetchTickets() {
        isLoading = true
        errorMessage = null

        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            isLoading = false
            return
        }

        val result = IssueApiClient.getMyIssues(token)
        result.onSuccess { issues ->
            val isFinishedView = ticketType == "FINISHED"
            val filteredIssues = issues.filter { 
                if (isFinishedView) it.status.uppercase() == "CLOSED" else it.status.uppercase() != "CLOSED"
            }
            tickets = filteredIssues.map { issue ->
                Ticket(
                    id = issue.id,
                    title = issue.title,
                    description = issue.description,
                    status = issue.status.toUiStatus()
                )
            }
        }.onFailure { throwable ->
            errorMessage = throwable.message ?: "Nie udało się pobrać listy zgłoszeń"
        }

        isLoading = false
    }

    LaunchedEffect(Unit) {
        fetchTickets()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    fetchTickets()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                    val titleText = if (ticketType == "FINISHED") "Zakończone Zgłoszenia" else "Aktualne zgłoszenia"
                    Text(
                        text = titleText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Wyloguj", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.padding(4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    val itemColor = if (ticketType == "FINISHED") androidx.compose.ui.graphics.Color(0xFF90D590) else androidx.compose.ui.graphics.Color(0xFFE4B560)
                    items(tickets) { ticket ->
                        val showGenerate = ticketType == "FINISHED"
                        TicketListItem(
                            ticket = ticket,
                            backgroundColor = itemColor,
                            showGenerateButton = showGenerate,
                            onGenerate = {
                                val token = AuthSessionStore.getToken(context)
                                if (token.isNullOrBlank()) {
                                    Toast.makeText(context, "Brak sesji użytkownika", Toast.LENGTH_SHORT).show()
                                    return@TicketListItem
                                }

                                IssueApiClient.enqueueRepairProtocolDownload(context, token, ticket.id)
                                    .onSuccess {
                                        Toast.makeText(context, "Rozpoczęto pobieranie", Toast.LENGTH_SHORT).show()
                                    }
                                    .onFailure {
                                        Toast.makeText(context, "Nie udało się rozpocząć pobierania", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onClick = { onTicketClick(ticket.id) }
                        )
                    }
                }
            }
        }
        
        pl.edu.ur.coopspace.user_module.UserBackButton(onBack = onBack)
    }
}



// 2. Komponent pojedynczego wiersza listy
@Composable
fun TicketListItem(
    ticket: Ticket,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.background,
    showGenerateButton: Boolean = false,
    onGenerate: () -> Unit = {},
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(backgroundColor)
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${ticket.title} | ${ticket.description}",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showGenerateButton) {
                    Button(
                        onClick = onGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
                    ) {
                        Text("Generuj", color = androidx.compose.ui.graphics.Color.Black, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = "Szczegóły",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.ui.graphics.Color.DarkGray
                )
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Szczegóły",
                    tint = androidx.compose.ui.graphics.Color.DarkGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    HorizontalDivider(color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
}
