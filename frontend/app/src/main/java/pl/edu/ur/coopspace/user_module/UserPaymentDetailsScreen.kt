package pl.edu.ur.coopspace.user_module

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun UserPaymentDetailsScreen(
    chargeId: Int,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var details by remember { mutableStateOf<UserChargeDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chargeId) {
        isLoading = true
        UserFinanceApiClient.getChargeDetails(context, chargeId)
            .onSuccess { res ->
                details = res
                isLoading = false
            }
            .onFailure { error ->
                errorMessage = error.message
                isLoading = false
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
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
                            .clickable { /* Drawer open if any */ }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Szczegóły opłaty",
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
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "Błąd: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
            } else {
                details?.let { chargeDetails ->
                    // Month
                    Text(text = "Miesiąc:", fontStyle = FontStyle.Italic, fontSize = 14.sp)
                    Text(text = chargeDetails.month, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Billing Period
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Okres Rozliczenia ", fontStyle = FontStyle.Italic, fontSize = 14.sp)
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(text = chargeDetails.periodStart, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
                    Text(text = chargeDetails.periodEnd, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status
                    Text(text = "Status", fontStyle = FontStyle.Italic, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val statusColor = if (chargeDetails.amountToPay == 0.0) Color(0xFF90D590) else Color(0xFFD6D18E)
                    val borderColor = if (chargeDetails.amountToPay > 0) Color(0xFFA53A3A) else Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(statusColor, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, borderColor, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = chargeDetails.statusText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                if (chargeDetails.amountToPay > 0) {
                                    val amountStr = String.format(Locale.US, "%.2f zł", chargeDetails.amountToPay)
                                    Text(text = "Do zapłaty: $amountStr", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                            if (chargeDetails.amountToPay > 0) {
                                Button(
                                    onClick = { showPaymentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    Text(text = "Zapłać", color = Color(0xFF6750A4), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Breakdown
                    Text(text = "Rozkład opłaty:", fontStyle = FontStyle.Italic, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF90D590), shape = RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            chargeDetails.items.forEach { item ->
                                val valStr = String.format(Locale.US, "%.2f zł", item.amount)
                                BreakdownRow(item.name, valStr)
                            }
                        }
                    }
                }
            }
        }

        if (showPaymentDialog && details != null) {
            PaymentDialog(
                initialAmount = details!!.amountToPay,
                onDismiss = { showPaymentDialog = false },
                onConfirm = { amount ->
                    showPaymentDialog = false
                    isLoading = true
                    coroutineScope.launch {
                        UserFinanceApiClient.postPayment(context, details!!.id, amount)
                            .onSuccess {
                                UserFinanceApiClient.getChargeDetails(context, chargeId)
                                    .onSuccess { res ->
                                        details = res
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

        UserBackButton(onBack = onBack)
    }
}

@Composable
fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.8f))
        Text(text = value, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.8f))
    }
}

@Composable
fun PaymentDialog(
    initialAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(String.format(Locale.US, "%.2f", initialAmount)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Zapłać") },
        text = {
            Column {
                Text(text = "Podaj kwotę do zapłaty:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amountText = newValue
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    onConfirm(amount)
                }
            }) {
                Text("Potwierdź")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

