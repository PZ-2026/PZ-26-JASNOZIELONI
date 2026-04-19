package pl.edu.ur.coopspace.administration_module

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Stars
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
import pl.edu.ur.coopspace.auth.AuthSessionStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminViewAnnouncementHistoryScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onAnnouncementClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val token = remember { AuthSessionStore.getToken(context) }
    val scope = rememberCoroutineScope()
    
    var announcements by remember { mutableStateOf<List<AnnouncementDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val checkedStates = remember { mutableStateListOf<Boolean>() }

    LaunchedEffect(token) {
        if (token != null) {
            AnnouncementApiClient.getAnnouncements(token)
                .onSuccess { data ->
                    announcements = data
                    checkedStates.clear()
                    checkedStates.addAll(List(data.size) { false })
                    isLoading = false
                }
                .onFailure {
                    isLoading = false
                    Toast.makeText(context, "Błąd pobierania ogłoszeń", Toast.LENGTH_SHORT).show()
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
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Nagłówek (Menu, Tytuł i przyciski akcji)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { /* Opcjonalne otwarcie szuflady */ }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Ogłoszenia···", // Tytuł zgodnie z makietą (z kropkami)
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

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
                        imageVector = Icons.Default.Build, // Placeholder z braku dedykowanej ikony
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Lista ogłoszeń (Zaimplementowana jako LazyColumn dla skalowalności)
        if (isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (announcements.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Brak ogłoszeń", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(announcements.size) { index ->
                    val ann = announcements[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ikona Gwiazdki
                        Icon(
                            imageVector = Icons.Outlined.Stars,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Tytuł i Data
                        val dateString = ann.createdAt.substringBefore("T")
                        Text(
                            text = "${ann.title} ($dateString)",
                            fontSize = 16.sp,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Checkbox
                        if (index < checkedStates.size) {
                            Checkbox(
                                checked = checkedStates[index],
                                onCheckedChange = { checkedStates[index] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF6750A4), // Klasyczny fiolet z Material 3
                                    uncheckedColor = Color.Gray
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Napis "Szczegóły" z przyciskiem/kliknięciem
                        Row(
                            modifier = Modifier.clickable { onAnnouncementClick(ann.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Szczegóły",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Szczegóły",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Przyciski akcji na dole (Usuń zaznaczone / Cofnij)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Przycisk Usuń zaznaczone z obramowaniem w stylu makiety
            OutlinedButton(
                onClick = {
                    if (token != null) {
                        val idsToDelete = announcements.indices
                            .filter { index -> checkedStates.getOrElse(index) { false } }
                            .map { index -> announcements[index].id }
                        
                        if (idsToDelete.isEmpty()) {
                            Toast.makeText(context, "Brak zaznaczonych elementów", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        
                        scope.launch {
                            isLoading = true
                            AnnouncementApiClient.deleteAnnouncements(token, idsToDelete)
                                .onSuccess {
                                    Toast.makeText(context, "Usunięto pomyślnie", Toast.LENGTH_SHORT).show()
                                    AnnouncementApiClient.getAnnouncements(token)
                                        .onSuccess { data ->
                                            announcements = data
                                            checkedStates.clear()
                                            checkedStates.addAll(List(data.size) { false })
                                            isLoading = false
                                        }.onFailure {
                                            isLoading = false
                                        }
                                }
                                .onFailure {
                                    isLoading = false
                                    Toast.makeText(context, "Błąd usuwania", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                border = null, // Brak widocznej ciemnej ramki na makiecie, głównie tło
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(text = "Usuń zaznaczone", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            }
            
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
