package pl.edu.ur.coopspace.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 1. Definiujemy kolory
val CoopPrimary = Color(0xFF3F51B5)
// ZMIANA: Z Color.White na nowy, miękki, jasnobłękitny odcień szarości
val CoopBackgroundSoft = Color(0xFFF7F8FC)
val CoopSurfaceVariant = Color(0xFFE8EAF6)
val CoopButtonBg = Color(0xFFF3E5F5)
val CoopTextSecondary = Color(0xFF757575)

// 2. Podpinamy je pod jasny motyw Material Design 3
private val LightColorScheme = lightColorScheme(
    primary = CoopPrimary,
    background = CoopBackgroundSoft, // <-- Używamy nowego tła
    surface = Color.White, // Karty (np. AdminCard) zostaną białe, żeby się wyróżniać
    surfaceVariant = CoopSurfaceVariant,
    secondaryContainer = CoopButtonBg,
    onSurfaceVariant = CoopTextSecondary
)

// 3. Propozycja kolorów dla Dark Mode (zostawiamy bez zmian)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7986CB),
    background = Color(0xFF121212),
    surfaceVariant = Color(0xFF303030),
    secondaryContainer = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFB0B0B0)
)

@Composable
fun CoopSpaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}