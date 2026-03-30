package pl.edu.ur.coopspace.administration_module

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Prosta klasa danych dla elementu menu
data class AdminMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit, // Callback do wylogowania
    onNavigateToAnnouncements: () -> Unit = {}, // Callback nawigacji do ogłoszeń
    onNavigateToUsers: () -> Unit = {}, // Callback nawigacji do użytkowników
    onNavigateToReports: () -> Unit = {}, // Callback nawigacji do zgłoszeń
    onNavigateToPaymentSettings: () -> Unit = {}, // Callback nawigacji do opłat
    onNavigateToRaports: () -> Unit = {} // Callback nawigacji do raportów
) {
    // 2. Definiujemy listę elementów menu na podstawie makiety
    val menuItems = listOf(
        AdminMenuItem("Tablica ogłoszeń", Icons.Default.Notifications) { onNavigateToAnnouncements() },
        AdminMenuItem("Zarządzaj użytkownikami", Icons.Default.Face) { onNavigateToUsers() },
        AdminMenuItem("Zarządzaj zgłoszeniami", Icons.Default.Build) { onNavigateToReports() },
        AdminMenuItem("Ustawienia opłat", Icons.Default.PriceCheck) { onNavigateToPaymentSettings() },
        AdminMenuItem("Raporty", Icons.Default.AssignmentTurnedIn) { onNavigateToRaports() },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 3. Nagłówek (Tytuł i przycisk wyloguj)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tytuł
            Text(
                text = "Administracja",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(start = 32.dp) // Offset by Title is centered
            )

            // Przycisk Wyloguj
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Wyloguj", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Lista kart menu
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(menuItems) { item ->
                AdminMenuCard(item)
            }
        }
    }
}

// 5. Komponent pojedynczej karty menu
@Composable
fun AdminMenuCard(item: AdminMenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tekst
            Text(
                text = item.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Ikona (Duża, po prawej stronie)
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary, // Kolorujemy ikony na kolor główny
                modifier = Modifier.size(64.dp)
            )
        }
    }
}
