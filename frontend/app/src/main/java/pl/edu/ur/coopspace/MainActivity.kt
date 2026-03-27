package pl.edu.ur.coopspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pl.edu.ur.coopspace.navigation.CoopSpaceApp // Importujemy funkcję z nawigacji
import pl.edu.ur.coopspace.ui.theme.CoopSpaceTheme // Importujemy Twój motyw

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoopSpaceTheme {
                // Wywołujemy poprawną nazwę funkcji z Twojego pliku AppNavigation.kt!
                CoopSpaceApp()
            }
        }
    }
}