package pl.edu.ur.coopspace.user_module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun UserFinancesScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onGenerateReport: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var balance by remember { mutableDoubleStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        UserFinanceApiClient.getSummary(context)
            .onSuccess { summary ->
                balance = summary.balance
                isLoading = false
            }
            .onFailure { error ->
                errorMessage = error.message
                isLoading = false
            }
    }

    val menuItems = listOf(
        UserMenuItem("Przeglądaj historię opłat", Icons.Default.Checklist) { onNavigateToHistory() },
        UserMenuItem("Generuj Raport Finansowy", Icons.AutoMirrored.Filled.ReceiptLong) { onGenerateReport() },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp))

                Text(
                    text = "Finanse",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Wyloguj", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.padding(4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (errorMessage != null) {
                Text(text = "Błąd: $errorMessage", color = MaterialTheme.colorScheme.error)
            } else {
                if (balance < 0) {
                    val amountDue = String.format(Locale.US, "%.2f zł", -balance)
                    FinanceStatusCard(
                        text = "Do zapłaty:",
                        amount = amountDue,
                        backgroundColor = Color(0xFFFD9734), // Orange
                        hasButton = true,
                        buttonText = "Zapłać",
                        onButtonClick = { showPaymentDialog = true }
                    )
                } else if (balance > 0) {
                    val overpayment = String.format(Locale.US, "%.2f zł", balance)
                    FinanceStatusCard(
                        text = "Nadpłata",
                        amount = overpayment,
                        backgroundColor = Color(0xFF90D590), // Greenish
                        hasButton = false
                    )
                } else {
                    FinanceStatusCard(
                        text = "Wszystko Opłacone",
                        amount = "",
                        backgroundColor = Color(0xFF90D590), // Greenish
                        hasButton = false,
                        isFullWidthText = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (showPaymentDialog) {
                PaymentDialog(
                    initialAmount = if (balance < 0) -balance else 0.0,
                    onDismiss = { showPaymentDialog = false },
                    onConfirm = { amount ->
                        showPaymentDialog = false
                        isLoading = true
                        coroutineScope.launch {
                            UserFinanceApiClient.postPayment(context, null, amount)
                                .onSuccess {
                                    UserFinanceApiClient.getSummary(context)
                                        .onSuccess { summary ->
                                            balance = summary.balance
                                            isLoading = false
                                        }
                                        .onFailure { error ->
                                            errorMessage = error.message
                                            isLoading = false
                                        }
                                }
                                .onFailure { error ->
                                    errorMessage = error.message
                                    isLoading = false
                                }
                        }
                    }
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(menuItems) { item ->
                    UserMenuCard(item)
                }
            }
        }

        UserBackButton(onBack = onBack)
    }
}

@Composable
fun FinanceStatusCard(
    text: String,
    amount: String,
    backgroundColor: Color,
    hasButton: Boolean,
    buttonText: String = "",
    isFullWidthText: Boolean = false,
    onButtonClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isFullWidthText) Arrangement.Center else Arrangement.SpaceBetween
        ) {
            if (isFullWidthText) {
                Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = amount, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (hasButton) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = onButtonClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text(text = buttonText, color = Color(0xFF6750A4), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
