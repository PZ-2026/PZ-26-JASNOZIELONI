package pl.edu.ur.coopspace.administration_module

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminGenerateStatisticRaportScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    var periodValue by remember { mutableFloatStateOf(6f) }
    
    val checklistItems = listOf(
        "Suma wszystkich wpłat",
        "Ogólna liczba usterek",
        "Średni czas rozwiązania usterek",
        "Ilość mieszkańców"
    )
    
    // Store checked state for each item
    val checkedStates = remember { 
        mutableStateMapOf<String, Boolean>().apply {
            checklistItems.forEach { this[it] = true } 
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

        // Nagłówek
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zgłoszeń",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )

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

        Spacer(modifier = Modifier.height(48.dp))

        // Instrukcja
        Text(
            text = "Wybierz dane z których\nwygenerowany zostanie\nraport",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 26.sp,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Checklista danych
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            checklistItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .clickable { checkedStates[item] = !(checkedStates[item] ?: false) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = checkedStates[item] ?: false,
                        onCheckedChange = { checkedStates[item] = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        // Sekcja okresu (suwak)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Okres",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "(miesiące)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Custom Period Selector (12 steps logic)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LabelsRow for 1, 3, 6, 9, 12
                Box(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                    listOf(1, 3, 6, 9, 12).forEach { period ->
                        val labelBias = ((period - 1) / 11f * 2) - 1
                        Text(
                            text = period.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier
                                .align(BiasAlignment(labelBias, 0f))
                                .clickable { periodValue = period.toFloat() },
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..12).forEach { step ->
                                Box(
                                    modifier = Modifier
                                        .size(if (step == 1 || step == 12) 6.dp else 2.dp)
                                        .background(
                                            if (step == 1 || step == 12) Color(0xFF6750A4) else Color.Gray.copy(alpha = 0.5f),
                                            RoundedCornerShape(50)
                                        )
                                        .clickable { periodValue = step.toFloat() }
                                )
                            }
                        }
                    }

                    // Handle
                    val bias = ((periodValue - 1) / 11f * 2) - 1
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(32.dp)
                            .align(BiasAlignment(bias, 0f))
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(2.dp, Color(0xFF6750A4), RoundedCornerShape(4.dp))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxHeight().width(3.dp),
                                thickness = DividerDefaults.Thickness,
                                color = Color(0xFF6750A4)
                            )
                            HorizontalDivider(
                                modifier = Modifier.fillMaxHeight().width(3.dp),
                                thickness = DividerDefaults.Thickness,
                                color = Color(0xFF6750A4)
                            )
                        }
                    }
                }

                Text(
                    text = "Wybrano: ${periodValue.toInt()} mies.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(48.dp))

        // Przyciski akcji
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(text = "Cofnij", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            }

            Button(
                onClick = { /* TODO: Generuj */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00801F)),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(text = "Generuj", color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
