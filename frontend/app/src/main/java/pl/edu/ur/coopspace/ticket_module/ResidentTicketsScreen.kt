package pl.edu.ur.coopspace.ticket_module

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
    onTicketClick: (Int) -> Unit, // Akcja po kliknięciu w konkretne zgłoszenie
    onAddNewTicketClick: () -> Unit, // Akcja po kliknięciu "Dodaj nowe zgłoszenie"
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
            tickets = issues.map { issue ->
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

    // Scaffold to "rusztowanie", które idealnie nadaje się do ekranów z pływającym przyciskiem (FAB)
    Scaffold(
        topBar = {
            // Nagłówek ekranu
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Moje zgłoszenia",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Wyloguj")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            // Przycisk "Dodaj nowe zgłoszenie" w prawym dolnym rogu
            ExtendedFloatingActionButton(
                onClick = onAddNewTicketClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dodaj nowe zgłoszenie")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(tickets) { ticket ->
                    TicketListItem(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

fun String.toUiStatus(): String {
    return when (this.uppercase()) {
        "OPEN" -> "Nowe"
        "IN_PROGRESS" -> "W trakcie"
        "CLOSED" -> "Zamkniete"
        else -> this
    }
}

// 2. Komponent pojedynczego wiersza listy
@Composable
fun TicketListItem(ticket: Ticket, onClick: () -> Unit) {
    // Używamy gotowego komponentu ListItem z Material 3 - robi za nas całą robotę z ułożeniem!
    ListItem(
        headlineContent = { 
            Text(text = ticket.title, fontWeight = FontWeight.Bold) 
        },
        supportingContent = { 
            Text(text = ticket.description) 
        },
        overlineContent = { 
            Text(text = ticket.status, color = MaterialTheme.colorScheme.primary) 
        },
        leadingContent = {
            // Ikona po lewej stronie (zgodnie z makietą kółko z gwiazdką - tutaj używamy ikony Star)
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            // Strzałka po prawej stronie
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Szczegóły",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.clickable { onClick() }
    )
}
