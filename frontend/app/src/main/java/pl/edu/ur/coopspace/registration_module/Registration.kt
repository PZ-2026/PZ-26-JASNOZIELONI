package pl.edu.ur.coopspace.registration_module

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.auth.AuthApiClient
import pl.edu.ur.coopspace.auth.AuthSessionStore
import pl.edu.ur.coopspace.network.BackendUrlStore

enum class UserRole { ADMINISTRATOR, MIESZKANIEC, KONSERWATOR }

data class AdminContact(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val address: String
)

private val adminContacts = listOf(
    AdminContact(1, "Anna", "Nowak", "+48 123 456 789", "anna.nowak@coopspace.pl", "ul. Spoldzielcza 1, Biuro 10"),
    AdminContact(2, "Jan", "Kowalski", "+48 987 654 321", "jan.kowalski@coopspace.pl", "ul. Spoldzielcza 1, Biuro 11")
)

@Composable
fun HeaderComponent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Logowanie", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "CoopSpace",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Budujmy wspólną przestrzeń",
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToAdmins: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit
) {
    // Domyślnie ustawiamy maila admina dla łatwego testowania
    var backendUrl by rememberSaveable { mutableStateOf(BackendUrlStore.getBaseUrl()) }
    var showBackendUrlDialog by remember { mutableStateOf(false) }
    var backendUrlDraft by rememberSaveable { mutableStateOf(backendUrl) }
    var login by remember { mutableStateOf("admin@test.com") }
    var password by remember { mutableStateOf("admin123") }
    val rememberMe = true
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        HeaderComponent()

        Button(
            onClick = {
                backendUrlDraft = backendUrl
                showBackendUrlDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Ustaw adres serwera", color = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = "Aktualny adres: $backendUrl",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 4.dp, bottom = 16.dp)
        )

        TextField(
            value = login,
            onValueChange = { 
                login = it
                errorMessage = null // Czyścimy błąd przy pisaniu
            },
            label = { Text("Email / Login") },
            isError = errorMessage != null,
            trailingIcon = {
                Icon(Icons.Default.Clear, contentDescription = "Wyczyść", modifier = Modifier.clickable { login = "" })
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Podaj adres e-mail przypisany do konta",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 4.dp, bottom = 16.dp)
        )

        TextField(
            value = password,
            onValueChange = { 
                password = it
                errorMessage = null
            },
            label = { Text("Hasło") },
            isError = errorMessage != null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, "Pokaż/ukryj hasło")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isLoading) return@Button

                BackendUrlStore.setBaseUrl(context, backendUrl)

                if (login.isBlank() || password.isBlank()) {
                    errorMessage = "Uzupełnij email i hasło"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                coroutineScope.launch {
                    val result = AuthApiClient.login(login.trim(), password)
                    isLoading = false

                    result.onSuccess { response ->
                        if (response.token.isBlank()) {
                            errorMessage = "Backend nie zwrocił tokenu"
                            return@onSuccess
                        }

                        if (rememberMe) {
                            AuthSessionStore.saveSession(
                                context = context,
                                token = response.token,
                                email = response.email,
                                role = response.role
                            )
                        } else {
                            AuthSessionStore.clearSession(context)
                        }

                        val appRole = mapBackendRoleToAppRole(response.role)
                        if (appRole != null) {
                            onLoginSuccess(appRole)
                        } else {
                            errorMessage = "Nieznana rola użytkownika: ${response.role ?: "brak"}"
                        }
                    }.onFailure { throwable ->
                        errorMessage = throwable.message ?: "Nie udało się zalogować"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(50),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(text = "Zaloguj", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 32.dp))
            }
        }

        if (showBackendUrlDialog) {
            AlertDialog(
                onDismissRequest = { showBackendUrlDialog = false },
                title = { Text(text = "Ustaw adres serwera") },
                text = {
                    Column {
                        Text(
                            text = "Wpisz IP lub pełny adres backendu. Jeśli nie podasz protokołu, zostanie dodane http://.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = backendUrlDraft,
                            onValueChange = {
                                backendUrlDraft = it
                                errorMessage = null
                            },
                            label = { Text("Adres serwera") },
                            placeholder = { Text("np. 192.168.0.10:8080") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            BackendUrlStore.setBaseUrl(context, backendUrlDraft)
                            backendUrl = BackendUrlStore.getBaseUrl()
                            showBackendUrlDialog = false
                        }
                    ) {
                        Text(text = "Zapisz")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBackendUrlDialog = false }) {
                        Text(text = "Anuluj")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .clickable { onNavigateToAdmins() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Jeśli jesteś nowym mieszkańcem spółdzielni i nie masz jeszcze konta kliknij tutaj by skontaktować się z administratorem",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun mapBackendRoleToAppRole(role: String?): UserRole? {
    return when (role?.uppercase()) {
        "ADMIN" -> UserRole.ADMINISTRATOR
        "RESIDENT" -> UserRole.MIESZKANIEC
        "MAINTAINER" -> UserRole.KONSERWATOR
        else -> null
    }
}

@Composable
fun AdminContactScreen(
    onBackClick: () -> Unit
) {
    val admins = adminContacts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        HeaderComponent()

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(admins) { admin ->
                AdminCard(admin = admin)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Cofnij", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}

@Composable
fun AdminCard(admin: AdminContact) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Administrator",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${admin.firstName} ${admin.lastName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Avatar", tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = admin.phone,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            val phone = admin.phone.filter { it.isDigit() || it == '+' }
                            if (phone.isNotBlank()) {
                                uriHandler.openUri("tel:$phone")
                            }
                        }
                    )
                    Text(text = admin.address, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(
                        text = admin.email,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            if (admin.email.isNotBlank()) {
                                uriHandler.openUri("mailto:${admin.email}")
                            }
                        }
                    )
                }
            }
        }
    }
}
