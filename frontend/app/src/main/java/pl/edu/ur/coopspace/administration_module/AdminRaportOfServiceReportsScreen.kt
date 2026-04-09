package pl.edu.ur.coopspace.administration_module

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun AdminRaportOfServiceReportsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    var periodValue by remember { mutableFloatStateOf(6f) }
    
    val categoryOptions = listOf("Wszystkie", "Hydraulika", "Elektryka", "Budowlane")
    val statusOptions = listOf("Nowe", "W trakcie", "Zamkniete")
    val workersOptions = listOf("Wszyscy", "(Tu z jakiejs listy wybor)")
    val buildingsOptions = listOf("Wszystkie", "(Tu z jakiejs listy wybor)")

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
                text = "Raport ze zgłoszeń",
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

        // Siatka filtrów 2x2
        Row(modifier = Modifier.fillMaxWidth()) {
            SelectionBox(
                label = "Ktegoria Usterki",
                options = categoryOptions,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(24.dp))
            SelectionBox(
                label = "Status Zgłoszenia",
                options = statusOptions,
                modifier = Modifier.weight(1f),
                borderColor = Color(0xFF2196F3) // Błękitny jak na makiecie dla wyróżnionego
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            SelectionBox(
                label = "Konserwator",
                options = workersOptions,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(24.dp))
            SelectionBox(
                label = "Budynek",
                options = buildingsOptions,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Suwak Okresu
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Okres",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "(miesiace)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Custom Period Selector matching the design with 12 steps
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LabelsRow for 1, 3, 6, 9, 12 (Main labels)
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            // Logic to find closest of 12 steps on click is hard without size, 
                            // so we'll stick to a simpler approach or just keep current selection.
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Background Track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
                        ) {
                            // Drawing 12 dots/ticks
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..12).forEach { step ->
                                    val isMain = step in listOf(1, 3, 6, 9, 12)
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
                    }

                    // The Handle / Marker (The double lines)
                    // value is 1..12. bias is -1..1.
                    val bias = ((periodValue - 1) / 11f * 2) - 1

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(32.dp)
                            .align(BiasAlignment(bias, 0f))
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(2.dp, Color(0xFF6750A4), RoundedCornerShape(4.dp))
                            .padding(vertical = 6.dp)
                            .clickable { /* Could add drag here */ },
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
                
                // Optional: display current value
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

@Composable
fun SelectionBox(
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF6750A4)
) {
    Box(modifier = modifier) {
        // Etykieta na górze
        Surface(
            modifier = Modifier
                .padding(start = 12.dp)
                .offset(y = (-10).dp)
                .zIndex(1f),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Główny kontener
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)), // Jasny fioletowy/biały tło
            shape = RoundedCornerShape(8.dp)
        ) {
            Column {
                // Header (z ikonką zamknij)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "(Wybierz)|",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp).clickable { /* Clear selection */ }
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = borderColor.copy(alpha = 0.5f))

                // Lista opcji
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    options.forEach { option ->
                        Text(
                            text = option,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.clickable { /* Select option */ }
                        )
                    }
                }
            }
        }
    }
}
