package pl.edu.ur.coopspace.ticket_module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
fun ServiceTicketDetailsScreen(
    ticketId: String,
    onBackClick: () -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var issue by remember { mutableStateOf<IssueDto?>(null) }
    var categoryNameById by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSavingStatus by remember { mutableStateOf(false) }
    
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }
    val statuses = listOf("OPEN", "IN_PROGRESS", "CLOSED")

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val parsedTicketId = ticketId.toIntOrNull()

    LaunchedEffect(ticketId) {
        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            isLoading = false
            return@LaunchedEffect
        }

        if (parsedTicketId == null) {
            errorMessage = "Nieprawidlowe id zgloszenia"
            isLoading = false
            return@LaunchedEffect
        }

        IssueApiClient.getIssueCategories(token)
            .onSuccess { categories ->
                categoryNameById = categories.associate { category -> category.id to category.name }
            }
            .onFailure { throwable ->
                errorMessage = throwable.message ?: "Nie udalo sie pobrac kategorii"
            }

        IssueApiClient.getAssignedIssues(token)
            .onSuccess { issues ->
                issue = issues.firstOrNull { it.id == parsedTicketId }
                if (issue == null) {
                    errorMessage = "Nie znaleziono zgloszenia"
                } else {
                    selectedStatus = issue?.status ?: "OPEN"
                }
            }
            .onFailure { throwable ->
                errorMessage = throwable.message ?: "Nie udalo sie pobrac szczegolow"
            }

        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Zgłoszenie $ticketId",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Tytuł zgłoszenia
            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(
                    text = "Tytuł zgłoszenia",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = issue?.title ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Opis zgłoszenia
            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(
                    text = "Opis zgłoszenia",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = issue?.description ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kategoria zgłoszenia
            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(
                    text = "Kategoria zgłoszenia",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = issue?.categoryId?.let { categoryId ->
                        categoryNameById[categoryId] ?: "Kategoria #$categoryId"
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (issue != null) {
                IssueImagesGallery(
                    issueId = issue!!.id,
                    managementEnabled = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Komentarz zgłoszenia
            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(
                    text = "Komentarz zgłoszenia",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Wypełnij komentarz\n(input field)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Purple40
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status zgłoszenia
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status zgłoszenia") },
                    modifier = Modifier.menuAnchor(type=MenuAnchorType.PrimaryEditable, enabled=true).fillMaxWidth(),
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
                        if (selectedStatus.isNotEmpty()) {
                            IconButton(onClick = { selectedStatus = "" }) {
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
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false },
                    modifier = Modifier.background(PurpleGrey80.copy(alpha = 0.3f))
                ) {
                    statuses.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(text = status.toStatusLabel()) },
                            onClick = {
                                selectedStatus = status
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edytuj zgłoszenie
            Button(
                onClick = {
                    if (isSavingStatus || issue == null) return@Button

                    val token = AuthSessionStore.getToken(context)
                    if (token.isNullOrBlank()) {
                        errorMessage = "Brak sesji. Zaloguj sie ponownie."
                        return@Button
                    }

                    isSavingStatus = true
                    errorMessage = null

                    coroutineScope.launch {
                        val result = IssueApiClient.updateIssueStatus(token, issue!!.id, selectedStatus)
                        isSavingStatus = false

                        result.onSuccess { updated ->
                            issue = updated
                            selectedStatus = updated.status
                        }.onFailure {
                            errorMessage = it.message ?: "Nie udalo sie zaktualizowac statusu"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp),
                enabled = !isSavingStatus && issue != null
            ) {
                if (isSavingStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Zapisz status")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun String.toStatusLabel(): String {
    return when (this.uppercase()) {
        "OPEN" -> "Nowe"
        "IN_PROGRESS" -> "W trakcie"
        "CLOSED" -> "Zamkniete"
        else -> this
    }
}
