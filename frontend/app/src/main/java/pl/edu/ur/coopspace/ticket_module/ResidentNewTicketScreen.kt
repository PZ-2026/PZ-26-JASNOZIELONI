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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.edu.ur.coopspace.ui.theme.Purple40
import pl.edu.ur.coopspace.ui.theme.PurpleGrey80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentNewTicketScreen(
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("Tekst") }
    var description by remember { mutableStateOf("Teskt") }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    val categories = listOf("Hydraulika", "Elektryka", "Wandalizm", "Inne")

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
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Szukaj")
                            Spacer(modifier = Modifier.width(8.dp))
                            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
                            text = { Text(text = category) },
                            onClick = {
                                selectedCategory = category
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
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Szukaj")
                            Spacer(modifier = Modifier.width(8.dp))
                            Divider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Wyślij zgłoszenie")
            }
        }
    }
}
