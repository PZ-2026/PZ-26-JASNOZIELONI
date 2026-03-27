package pl.edu.ur.coopspace.ticket_module

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTicketsScreen(
    onTicketClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Zgłoszenia do naprawy",
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
        // Konserwator może nie potrzebować FAB do dodawania, ale możemy mu dać ikonę warsztatu
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Filtrowanie lub statusy */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Build, contentDescription = "Narzędzia")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Korzystamy z tych samych dummyTickets co ResidentTicketsScreen
            items(dummyTickets) { ticket ->
                TicketListItem(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}
