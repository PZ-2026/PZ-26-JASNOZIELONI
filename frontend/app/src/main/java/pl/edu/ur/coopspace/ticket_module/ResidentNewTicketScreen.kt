package pl.edu.ur.coopspace.ticket_module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.auth.AuthSessionStore
import pl.edu.ur.coopspace.ui.theme.Purple40
import pl.edu.ur.coopspace.ui.theme.PurpleGrey80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentNewTicketScreen(
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<IssueCategoryDto>>(emptyList()) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            return@LaunchedEffect
        }

        IssueApiClient.getIssueCategories(token)
            .onSuccess { categories = it }
            .onFailure { errorMessage = it.message ?: "Nie udalo sie pobrac kategorii" }
    }

    var locationExpanded by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("") }
    val locations = listOf("Mieszkanie", "Inne miejsce\n(Dodaj do opisu)")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Nowe zgłoszenie",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu/Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PurpleGrey80.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profil",
                                tint = Purple40,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onBackClick,
                containerColor = Color(0xFFF7F2FA),
                contentColor = Purple40,
            ) {
                Text(
                    text = "Cofnij",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tytuł zgłoszenia
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tytuł zgłoszenia", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = PurpleGrey80.copy(alpha = 0.5f),
                    focusedContainerColor = PurpleGrey80.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp),
                trailingIcon = {
                    if (title.isNotEmpty()) {
                        IconButton(onClick = { title = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Wyczyść"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Opis zgłoszenia
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis zgłoszenia", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(0.85f).height(100.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = PurpleGrey80.copy(alpha = 0.5f),
                    focusedContainerColor = PurpleGrey80.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp),
                trailingIcon = {
                    if (description.isNotEmpty()) {
                        IconButton(onClick = { description = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Wyczyść"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Kategoria
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategoria") },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth(),
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Szukaj")
                            Spacer(modifier = Modifier.width(8.dp))
                            HorizontalDivider(
                                modifier = Modifier.height(24.dp).width(1.dp),
                                thickness = DividerDefaults.Thickness,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    },
                    trailingIcon = {
                        if (selectedCategory.isNotEmpty()) {
                            IconButton(onClick = { selectedCategory = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Wyczyść")
                            }
                        } else {
                            // Jeśli chcemy pokazać ikonę rozwijania po prawej, to ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Purple40,
                        focusedBorderColor = Purple40,
                        unfocusedLabelColor = Purple40,
                        focusedLabelColor = Purple40
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(PurpleGrey80.copy(alpha = 0.3f))
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = category.name) },
                            onClick = {
                                selectedCategory = category.name
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Miejsce
            ExposedDropdownMenuBox(
                expanded = locationExpanded,
                onExpandedChange = { locationExpanded = !locationExpanded },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                OutlinedTextField(
                    value = selectedLocation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Miejsce") },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth(),
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Szukaj")
                            Spacer(modifier = Modifier.width(8.dp))
                            HorizontalDivider(
                                modifier = Modifier.height(24.dp).width(1.dp),
                                thickness = DividerDefaults.Thickness,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    },
                    trailingIcon = {
                        if (selectedLocation.isNotEmpty()) {
                            IconButton(onClick = { selectedLocation = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Wyczyść")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Purple40,
                        focusedBorderColor = Purple40,
                        unfocusedLabelColor = Purple40,
                        focusedLabelColor = Purple40
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                ExposedDropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false },
                    modifier = Modifier.background(PurpleGrey80.copy(alpha = 0.3f))
                ) {
                    locations.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(text = location) },
                            onClick = {
                                selectedLocation = location
                                locationExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Dodaj zdjęcie
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Image, contentDescription = "Aparat/Zdjęcie")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dodaj zdjęcie")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wyślij zgłoszenie
            Button(
                onClick = {
                    if (isLoading) return@Button

                    if (title.isBlank() || description.isBlank() || selectedCategory.isBlank()) {
                        errorMessage = "Uzupelnij tytul, opis i kategorie"
                        return@Button
                    }

                    val selectedCategoryId = categories.firstOrNull { it.name == selectedCategory }?.id
                    if (selectedCategoryId == null) {
                        errorMessage = "Wybierz poprawna kategorie"
                        return@Button
                    }

                    val token = AuthSessionStore.getToken(context)
                    if (token.isNullOrBlank()) {
                        errorMessage = "Brak sesji. Zaloguj sie ponownie."
                        return@Button
                    }

                    errorMessage = null
                    isLoading = true
                    coroutineScope.launch {
                        val result = IssueApiClient.createIssue(
                            token = token,
                            title = title.trim(),
                            description = description.trim(),
                            categoryId = selectedCategoryId
                        )

                        isLoading = false
                        result.onSuccess {
                            onBackClick()
                        }.onFailure {
                            errorMessage = it.message ?: "Nie udalo sie wyslac zgloszenia"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Wyślij zgłoszenie")
                }
            }
        }
    }
}
