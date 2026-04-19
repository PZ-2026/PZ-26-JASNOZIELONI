package pl.edu.ur.coopspace.administration_module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentSettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // Stany dla pól tekstowych
    var rentValue by remember { mutableStateOf("") }
    var waterValue by remember { mutableStateOf("") }
    var electricityValue by remember { mutableStateOf("") }
    var gasValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isLoading = true
        PaymentApiClient.getRates(context).onSuccess { rates ->
            rentValue = rates.rentRate
            waterValue = rates.waterRate
            electricityValue = rates.electricityRate
            gasValue = rates.gasRate
            isLoading = false
        }.onFailure { error ->
            isLoading = false
            Toast.makeText(context, error.message ?: "Błąd podczas pobierania stawek", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Nagłówek (Tytuł i przyciski akcji)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tytuł
            Text(
                text = "Opłaty",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Przycisk Wyloguj
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

                // Avatar / Ikonka profilu
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build, // Placeholder z braku dedykowanej ikony
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Pola formularza
        PaymentInputField(
            label = "Czynsz",
            value = rentValue,
            onValueChange = { rentValue = it },
            onClear = { rentValue = "" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        PaymentInputField(
            label = "Stawka za wode",
            value = waterValue,
            onValueChange = { waterValue = it },
            onClear = { waterValue = "" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        PaymentInputField(
            label = "Stawka za prąd",
            value = electricityValue,
            onValueChange = { electricityValue = it },
            onClear = { electricityValue = "" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        PaymentInputField(
            label = "Stawka za gaz",
            value = gasValue,
            onValueChange = { gasValue = it },
            onClear = { gasValue = "" }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // Przyciski akcji (Anuluj / Zapisz)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF02020)),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(text = "Anuluj", color = Color.White, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    if (isLoading) return@Button
                    isLoading = true
                    coroutineScope.launch {
                        PaymentApiClient.updateRates(
                            context = context,
                            rentRate = rentValue,
                            waterRate = waterValue,
                            electricityRate = electricityValue,
                            gasRate = gasValue
                        ).onSuccess {
                            isLoading = false
                            Toast.makeText(context, "Stawki zostały zapisane pomyślnie", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }.onFailure { error ->
                            isLoading = false
                            Toast.makeText(context, error.message ?: "Błąd zapisu", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009922)),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(text = if (isLoading) "Zapisywanie..." else "Zapisz", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PaymentInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEBE6F3),
                unfocusedContainerColor = Color(0xFFEBE6F3),
                disabledContainerColor = Color(0xFFEBE6F3),
                focusedIndicatorColor = Color.Gray,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            trailingIcon = {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Wyczyść"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
    }
}
