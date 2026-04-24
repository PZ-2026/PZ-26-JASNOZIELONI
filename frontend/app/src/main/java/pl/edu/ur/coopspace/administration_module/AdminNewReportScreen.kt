package pl.edu.ur.coopspace.administration_module

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.auth.AuthSessionStore
import pl.edu.ur.coopspace.ticket_module.IssueApiClient
import pl.edu.ur.coopspace.ticket_module.IssueCategoryDto
import pl.edu.ur.coopspace.ui.theme.Purple40
import pl.edu.ur.coopspace.ui.theme.PurpleGrey80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNewReportScreen(
    onBackClick: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<IssueCategoryDto>>(emptyList()) }
    
    var locationExpanded by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("") }
    val locations = listOf("Miejsce wspólne", "Inne (Dodaj do opisu)")
    
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedIssueId by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = (selectedImageUris + uris).distinct()
    }

    LaunchedEffect(Unit) {
        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            return@LaunchedEffect
        }

        IssueApiClient.getIssueCategories(token)
            .onSuccess { categories = it }
            .onFailure { errorMessage = it.message ?: "Nie udało się pobrać kategorii" }
    }

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

            // Opis zgłoszenia
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis zgłoszenia", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(0.85f).height(64.dp),
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
                    label = { Text("Kategoria", fontSize = 12.sp) },
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
                    label = { Text("Miejsce", fontSize = 12.sp) },
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

            Spacer(modifier = Modifier.height(32.dp))

            // Dodaj zdjęcie
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(200.dp).height(48.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Image, contentDescription = "Aparat/Zdjęcie")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedImageUris.isEmpty()) "Dodaj zdjęcie" else "Zdjęcia: ${selectedImageUris.size}")
            }

            if (selectedImageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Wybrano ${selectedImageUris.size} zdjęć", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(onClick = { selectedImageUris = emptyList() }) {
                        Text("Wyczyść")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wyślij zgłoszenie
            Button(
                onClick = {
                    if (isLoading) return@Button
                    
                    if (description.isBlank() || selectedCategory.isBlank() || selectedLocation.isBlank()) {
                        errorMessage = "Wypełnij wszystkie pola"
                        return@Button
                    }
                    
                    val selectedCategoryId = categories.firstOrNull { it.name == selectedCategory }?.id
                    if (selectedCategoryId == null) {
                        errorMessage = "Wybierz poprawną kategorie"
                        return@Button
                    }

                    val token = AuthSessionStore.getToken(context)
                    if (token.isNullOrBlank()) {
                        errorMessage = "Brak sesji. Zaloguj się ponownie."
                        return@Button
                    }

                    errorMessage = null
                    isLoading = true

                    coroutineScope.launch {
                        val titleStr = "[$selectedLocation] Zgłoszenie administracyjne"
                        
                        val result = IssueApiClient.createIssue(
                            token = token,
                            title = titleStr,
                            description = description.trim(),
                            categoryId = selectedCategoryId
                        )

                        isLoading = false
                        result.onSuccess { issue ->
                            uploadedIssueId = issue.id
                            if (selectedImageUris.isEmpty()) {
                                onBackClick()
                                return@launch
                            }

                            var uploadFailed = false
                            for (uri in selectedImageUris) {
                                val uploadResult = IssueApiClient.uploadIssueImage(
                                    token = token,
                                    issueId = issue.id,
                                    context = context,
                                    uri = uri
                                )

                                if (uploadResult.isFailure) {
                                    uploadFailed = true
                                    errorMessage = uploadResult.exceptionOrNull()?.message ?: "Nie udało się zapisać zdjęć"
                                    break
                                }
                            }

                            if (!uploadFailed) {
                                selectedImageUris = emptyList()
                                onBackClick()
                            }
                        }.onFailure { err ->
                            errorMessage = err.message ?: "Nie udało się wysłać zgłoszenia"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(200.dp).height(48.dp),
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
