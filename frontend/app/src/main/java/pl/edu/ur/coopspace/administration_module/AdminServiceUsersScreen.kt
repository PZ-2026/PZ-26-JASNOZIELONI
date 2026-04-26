package pl.edu.ur.coopspace.administration_module

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.auth.AuthSessionStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServiceUsersScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedActivityFilter by remember { mutableStateOf("ALL") }
    var maintainers by remember { mutableStateOf<List<AdminUserDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showAddDialog by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var updatingUserId by remember { mutableStateOf<Int?>(null) }

    suspend fun loadMaintainers() {
        isLoading = true
        errorMessage = null

        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj się ponownie."
            isLoading = false
            return
        }

        UserAdminApiClient.getMaintainers(token)
            .onSuccess { maintainers = it }
            .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udało się pobrać listy konserwatorów" }

        isLoading = false
    }

    suspend fun updateMaintainerState(maintainer: AdminUserDto, isActive: Boolean) {
        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            return
        }

        updatingUserId = maintainer.id
        errorMessage = null

        UserAdminApiClient.updateUserActiveState(token, maintainer.id, isActive)
            .onSuccess { loadMaintainers() }
            .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udało się zmienić stanu konta" }

        updatingUserId = null
    }

    LaunchedEffect(Unit) {
        loadMaintainers()
    }

    val filteredMaintainers = maintainers.filter { maintainer ->
        val matchesSearch = searchQuery.isBlank() || "${maintainer.firstName} ${maintainer.lastName} ${maintainer.email}".contains(searchQuery, ignoreCase = true)
        val matchesActivity = when (selectedActivityFilter) {
            "ACTIVE" -> maintainer.isActive
            "INACTIVE" -> !maintainer.isActive
            else -> true
        }

        matchesSearch && matchesActivity
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCreating) showAddDialog = false },
            title = { Text("Dodaj konserwatora") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Imie") }, singleLine = true)
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Nazwisko") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Haslo") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefon") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isCreating) return@Button
                        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMessage = "Uzupełnij wszystkie wymagane pola"
                            return@Button
                        }

                        val token = AuthSessionStore.getToken(context)
                        if (token.isNullOrBlank()) {
                            errorMessage = "Brak sesji. Zaloguj się ponownie."
                            return@Button
                        }

                        isCreating = true
                        errorMessage = null
                        coroutineScope.launch {
                            UserAdminApiClient.createMaintainer(
                                token = token,
                                email = email.trim(),
                                password = password,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                phoneNumber = phone.ifBlank { null }
                            ).onSuccess {
                                showAddDialog = false
                                firstName = ""
                                lastName = ""
                                email = ""
                                password = ""
                                phone = ""
                                loadMaintainers()
                            }.onFailure { throwable ->
                                errorMessage = throwable.message ?: "Nie udało się utworzyć konta konserwatora"
                            }
                            isCreating = false
                        }
                    },
                    enabled = !isCreating
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Utworz")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isCreating) showAddDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
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
                    text = "Konserwatorzy",
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

        Spacer(modifier = Modifier.height(32.dp))

        // Pole wyszukiwania
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Wyszukaj konserwatora", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Menu, // Na makiecie jest ikona podobna do menu (lista/filtry) po lewej
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

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedActivityFilter == "ALL",
                onClick = { selectedActivityFilter = "ALL" },
                label = { Text("Wszystkie") }
            )
            FilterChip(
                selected = selectedActivityFilter == "ACTIVE",
                onClick = { selectedActivityFilter = "ACTIVE" },
                label = { Text("Aktywne") }
            )
            FilterChip(
                selected = selectedActivityFilter == "INACTIVE",
                onClick = { selectedActivityFilter = "INACTIVE" },
                label = { Text("Nieaktywne") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Lista konserwatorów
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(filteredMaintainers.size) { index ->
                val maintainer = filteredMaintainers[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${maintainer.firstName} ${maintainer.lastName} | ${maintainer.email}",
                            fontSize = 16.sp,
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (maintainer.isActive) "Konto aktywne" else "Konto dezaktywowane",
                            fontSize = 12.sp,
                            color = if (maintainer.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                updateMaintainerState(maintainer, !maintainer.isActive)
                            }
                        },
                        enabled = updatingUserId != maintainer.id,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (maintainer.isActive) Color(0xFFC62828) else Color(0xFF2E7D32)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (updatingUserId == maintainer.id) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text(if (maintainer.isActive) "Dezaktywuj" else "Aktywuj")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przyciski akcji na dole (Dodaj Konserwatora / Cofnij)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(text = "Dodaj Konserwatora", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
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
