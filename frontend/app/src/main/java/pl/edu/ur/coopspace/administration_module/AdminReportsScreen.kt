package pl.edu.ur.coopspace.administration_module

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminReportsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToFinishedReports: () -> Unit = {},
    onNavigateToReportsInProgress: () -> Unit = {},
    onNavigateToNewReport: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Nagłówek (Tytuł i przyciski akcji)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tytuł
            Text(
                text = "Zgłoszenia",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Przycisk Wyloguj
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

                // Avatar / Ikonka profilu
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build, // Placeholder z braku dedykowanej ikony, ew. Person
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Karta 1
        ReportCard(
            title = "Dodaj ogólnowspólnotowe\nzgłoszenie",
            iconContent = {
                // Zastąpienie bardzo dużą ikoną
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(130.dp).offset(x = 0.dp)
                )
            },
            onClick = { onNavigateToNewReport() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Karta 2
        ReportCard(
            title = "Przeglądaj aktualne\nzgłoszenia",
            iconContent = {
                // Ikona edycji
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(110.dp).offset(x = 0.dp)
                )
            },
            onClick = { onNavigateToReportsInProgress() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Karta 3
        ReportCard(
            title = "Przeglądaj zakończone\nzgłoszenia",
            iconContent = {
                // Duża ikona zatwierdzenia (Check)
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(140.dp).offset(x = 10.dp)
                )
            },
            onClick = { onNavigateToFinishedReports() }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Przycisk Cofnij
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text(text = "Cofnij", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ReportCard(
    title: String,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFCF5FD) // Jasny róż tak sam jak w AnnouncementScreen/UsersScreen
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f),
                lineHeight = 22.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.CenterEnd
            ) {
                iconContent()
            }
        }
    }
}
