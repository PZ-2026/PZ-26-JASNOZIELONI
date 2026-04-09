package pl.edu.ur.coopspace.administration_module

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import pl.edu.ur.coopspace.ticket_module.IssueApiClient
import pl.edu.ur.coopspace.ticket_module.toUiStatus

enum class InProgressReportStatus { ASSIGNED, UNASSIGNED }

data class ReportInProgressItem(
    val id: Int,
    val title: String,
    val status: InProgressReportStatus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsInProgressScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onTicketClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var localFilterText by remember { mutableStateOf("") }
    var statusFilterExpanded by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var reports by remember { mutableStateOf<List<ReportInProgressItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val statusFilterOptions = listOf("ALL", "OPEN", "IN_PROGRESS")

    suspend fun fetchReports(status: String?, localId: Int?) {
        isLoading = true
        errorMessage = null

        val token = AuthSessionStore.getToken(context)
        if (token.isNullOrBlank()) {
            errorMessage = "Brak sesji. Zaloguj sie ponownie."
            isLoading = false
            return
        }

        IssueApiClient.getAllIssues(token, status = status, localId = localId)
            .onSuccess { issues ->
                reports = issues
                    .filter { it.status != "CLOSED" }
                    .map { issue ->
                        ReportInProgressItem(
                            id = issue.id,
                            title = "${issue.title} | Lokal ${issue.localId ?: "-"}",
                            status = if (issue.status == "IN_PROGRESS") InProgressReportStatus.ASSIGNED else InProgressReportStatus.UNASSIGNED
                        )
                    }
            }
            .onFailure { throwable ->
                errorMessage = throwable.message ?: "Nie udalo sie pobrac zgloszen"
            }

        isLoading = false
    }

    LaunchedEffect(Unit) {
        fetchReports(status = null, localId = null)
    }

    val filteredReports = reports.filter {
        searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Nagłówek
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
                        .clickable { /* TODO */ }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Zgłoszenia",
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

        Spacer(modifier = Modifier.height(32.dp))

        // Pole wyszukiwania
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Wyszukaj mieszkanca", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Menu,
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

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = statusFilterExpanded,
                onExpandedChange = { statusFilterExpanded = !statusFilterExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = if (selectedStatusFilter == "ALL") "Wszystkie statusy" else selectedStatusFilter.toUiStatus(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusFilterExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = statusFilterExpanded,
                    onDismissRequest = { statusFilterExpanded = false }
                ) {
                    statusFilterOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(if (status == "ALL") "Wszystkie statusy" else status.toUiStatus()) },
                            onClick = {
                                selectedStatusFilter = status
                                statusFilterExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = localFilterText,
                onValueChange = { localFilterText = it.filter(Char::isDigit) },
                label = { Text("Lokal") },
                singleLine = true,
                modifier = Modifier.width(110.dp)
            )

            Button(
                onClick = {
                    val statusParam = selectedStatusFilter.takeUnless { it == "ALL" }
                    val localParam = localFilterText.takeIf { it.isNotBlank() }?.toIntOrNull()
                    if (localFilterText.isNotBlank() && localParam == null) {
                        errorMessage = "Numer lokalu musi byc liczba"
                        return@Button
                    }

                    coroutineScope.launch {
                        fetchReports(status = statusParam, localId = localParam)
                    }
                }
            ) {
                Text("Filtruj")
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column {
                    filteredReports.forEachIndexed { index, report ->
                        val bgColor = if (report.status == InProgressReportStatus.ASSIGNED) {
                            Color(0xFF9ACDE7)
                        } else {
                            Color(0xFFDFB45E)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .clickable { onTicketClick(report.id) }
                                .padding(horizontal = 24.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = report.title,
                                fontSize = 15.sp,
                                color = Color.Black
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Szczegoly",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Wiecej",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (index < filteredReports.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Przycisk Cofnij
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
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
