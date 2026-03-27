package pl.edu.ur.coopspace.registration_module

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Usunięto zdefiniowane tu na sztywno kolory. Będziemy korzystać z MaterialTheme.

enum class UserRole { ADMINISTRATOR, MIESZKANIEC, KONSERWATOR }

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val phone: String,
    val email: String,
    val address: String
)

val pseudoDatabase = listOf(
    User(1, "Anna", "Nowak", UserRole.ADMINISTRATOR, "+48 123 456 789", "anna.nowak@coopspace.pl", "ul. Spółdzielcza 1, Biuro 10"),
    User(2, "Jan", "Kowalski", UserRole.ADMINISTRATOR, "+48 987 654 321", "jan.kowalski@coopspace.pl", "ul. Spółdzielcza 1, Biuro 11"),
    User(3, "Mietek", "Kozidrak", UserRole.MIESZKANIEC, "+48 111 222 333", "mietek@kozidrak.pl", "ul. Polna 4/12"),
    User(4, "Zbigniew", "Złota-Rączka", UserRole.KONSERWATOR, "+48 555 444 333", "usterki@coopspace.pl", "Warsztat bud. B")
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
            color = MaterialTheme.colorScheme.primary // Bierzemy kolor z motywu
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
    onNavigateToAdmins: () -> Unit
) {
    var login by remember { mutableStateOf("Mietek@Kozidrak") }
    var password by remember { mutableStateOf("****") }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        HeaderComponent()

        TextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login") },
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
            text = "Podaj login otrzymany od administratora spółdzielni",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 4.dp, bottom = 16.dp)
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            trailingIcon = {
                Icon(Icons.Default.Clear, contentDescription = "Wyczyść", modifier = Modifier.clickable { password = "" })
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Text(text = "Zapamiętaj mnie", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "Przypomnij hasło",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* TODO: Akcja logowania */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(50)
        ) {
            Text(text = "Zaloguj", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 32.dp))
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
                text = "Jeśli jesteś nowym mieszkańcem spółdzielni i nie masz jeszcze konta skontaktuj się z administratorem lub kliknij tutaj",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminContactScreen(
    onBackClick: () -> Unit
) {
    val admins = pseudoDatabase.filter { it.role == UserRole.ADMINISTRATOR }

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
fun AdminCard(admin: User) {
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
                    Text(text = admin.phone, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text(text = admin.address, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(text = admin.email, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}