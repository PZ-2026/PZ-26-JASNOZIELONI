package pl.edu.ur.coopspace.administration_module

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
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
fun AdminAnnouncementDetailsScreen(
    announcementId: Int,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val token = remember { AuthSessionStore.getToken(context) }
    
    var announcement by remember { mutableStateOf<AnnouncementDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(token, announcementId) {
        if (token != null) {
            AnnouncementApiClient.getAnnouncementById(token, announcementId)
                .onSuccess { data ->
                    announcement = data
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Błąd pobierania ogłoszenia", Toast.LENGTH_SHORT).show()
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Nagłówek (Back, Tytuł i przyciski akcji)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cofnij",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onNavigateBack() }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Szczegóły ogłoszenia",
                    fontSize = 22.sp,
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

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build, 
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (announcement != null) {
            val dateString = announcement!!.createdAt.substringBefore("T")
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEBE6F3), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = announcement!!.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Dodano: $dateString",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = announcement!!.content,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        } else {
            Text(text = "Nie znaleziono ogłoszenia", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Przycisk Cofnij
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text(text = "Cofnij", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
