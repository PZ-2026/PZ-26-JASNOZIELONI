package pl.edu.ur.coopspace.administration_module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.edu.ur.coopspace.auth.AuthSessionStore
import pl.edu.ur.coopspace.ticket_module.IssueApiClient
import pl.edu.ur.coopspace.ticket_module.IssueDto
import pl.edu.ur.coopspace.ticket_module.MaintainerDto
import pl.edu.ur.coopspace.ticket_module.IssueImagesGallery
import pl.edu.ur.coopspace.ticket_module.toUiStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIssueDetailsScreen(
    ticketId: Int,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var issue by remember { mutableStateOf<IssueDto?>(null) }
    var categories by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var maintainers by remember { mutableStateOf<List<MaintainerDto>>(emptyList()) }

    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("OPEN") }

    var assigneeExpanded by remember { mutableStateOf(false) }
    var selectedAssigneeId by remember { mutableStateOf<Int?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var isSavingStatus by remember { mutableStateOf(false) }
    var isAssigning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val statusOptions = listOf("OPEN", "IN_PROGRESS", "CLOSED")

    LaunchedEffect(ticketId) {
        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            isLoading = false
            return@LaunchedEffect
        }

        IssueApiClient.getIssueCategories(token)
            .onSuccess { list -> categories = list.associate { it.id to it.name } }
            .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udalo sie pobrac kategorii" }

        IssueApiClient.getMaintainers(token)
            .onSuccess { list -> maintainers = list }
            .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udalo sie pobrac konserwatorow" }

        IssueApiClient.getAllIssues(token)
            .onSuccess { issues ->
                issue = issues.firstOrNull { it.id == ticketId }
                if (issue == null) {
                    errorMessage = "Nie znaleziono zgloszenia"
                } else {
                    selectedStatus = issue?.status ?: "OPEN"
                    selectedAssigneeId = null
                }
            }
            .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udalo sie pobrac zgloszenia" }

        isLoading = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Szczegoly zgloszenia", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onLogout) { Text("Wyloguj") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            issue?.let { currentIssue ->
                Text(text = "ID: ${currentIssue.id}", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                IssueImagesGallery(
                    issueId = currentIssue.id,
                    managementEnabled = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentIssue.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tytul") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentIssue.description,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Opis") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentIssue.categoryId?.let { categories[it] ?: "Kategoria #$it" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategoria") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentIssue.status.toUiStatus(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Aktualny status") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus.toUiStatus(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nowy status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .menuAnchor(type=MenuAnchorType.PrimaryEditable, enabled=true)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { status ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(status.toUiStatus()) },
                                onClick = {
                                    selectedStatus = status
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val token = AuthSessionStore.getToken(context) ?: return@Button
                        isSavingStatus = true
                        errorMessage = null
                        coroutineScope.launch {
                            IssueApiClient.updateIssueStatus(token, currentIssue.id, selectedStatus)
                                .onSuccess { updated ->
                                    issue = updated
                                    snackbarHostState.showSnackbar("Status zgloszenia zostal zapisany")
                                }
                                .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udalo sie zapisac statusu" }
                            isSavingStatus = false
                        }
                    },
                    enabled = !isSavingStatus,
                    colors = ButtonDefaults.buttonColors()
                ) {
                    if (isSavingStatus) {
                        CircularProgressIndicator(modifier = Modifier.height(16.dp))
                    } else {
                        Text("Zapisz status")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ExposedDropdownMenuBox(
                    expanded = assigneeExpanded,
                    onExpandedChange = { assigneeExpanded = !assigneeExpanded }
                ) {
                    OutlinedTextField(
                        value = maintainers.firstOrNull { it.id == selectedAssigneeId }?.let { "${it.firstName} ${it.lastName}" }
                            ?: "Wybierz konserwatora",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Przypisz konserwatora") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                        modifier = Modifier
                            .menuAnchor(type=MenuAnchorType.PrimaryEditable, enabled=true)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = assigneeExpanded,
                        onDismissRequest = { assigneeExpanded = false }
                    ) {
                        maintainers.forEach { maintainer ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("${maintainer.firstName} ${maintainer.lastName}") },
                                onClick = {
                                    selectedAssigneeId = maintainer.id
                                    assigneeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val token = AuthSessionStore.getToken(context) ?: return@Button
                        val assigneeId = selectedAssigneeId ?: return@Button
                        isAssigning = true
                        errorMessage = null
                        coroutineScope.launch {
                            IssueApiClient.assignIssue(token, currentIssue.id, assigneeId)
                                .onSuccess { updated ->
                                    issue = updated
                                    snackbarHostState.showSnackbar("Konserwator zostal przypisany")
                                }
                                .onFailure { throwable -> errorMessage = throwable.message ?: "Nie udalo sie przypisac konserwatora" }
                            isAssigning = false
                        }
                    },
                    enabled = !isAssigning && selectedAssigneeId != null
                ) {
                    if (isAssigning) {
                        CircularProgressIndicator(modifier = Modifier.height(16.dp))
                    } else {
                        Text("Przypisz")
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBackClick) {
            Text("Cofnij")
        }
    }
    }
}
