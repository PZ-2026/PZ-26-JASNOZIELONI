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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Pseudo-model danych dla zgłoszenia
data class Ticket(
    val id: Int,
    val title: String,
    val description: String,
    val status: String // To posłuży jako "Overline" z makiety
)

// Generujemy 8 przykładowych zgłoszeń
val dummyTickets = List(8) { index ->
    Ticket(
        id = index + 1,
        title = "Zgłoszenie ${index + 1}",
        description = "Krótki opis zgłoszenia numer ${index + 1}",
        status = if (index % 2 == 0) "W trakcie" else "Nowe" // Zmienne statusy dla urozmaicenia
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentTicketsScreen(
    onTicketClick: (Int) -> Unit, // Akcja po kliknięciu w konkretne zgłoszenie
    onAddNewTicketClick: () -> Unit, // Akcja po kliknięciu "Dodaj nowe zgłoszenie"
    onLogout: () -> Unit // Dodajemy akcję wylogowania
) {
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
        // Lista zgłoszeń
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold podaje padding, żeby lista nie wjechała pod przycisk
        ) {
            items(dummyTickets) { ticket ->
                TicketListItem(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) // Linia oddzielająca
            }
        }
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
