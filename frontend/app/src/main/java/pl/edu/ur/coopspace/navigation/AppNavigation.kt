package pl.edu.ur.coopspace.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.coopspace.registration_module.AdminContactScreen
import pl.edu.ur.coopspace.registration_module.LoginScreen

@Composable
fun CoopSpaceApp() {
    // 1. Tworzymy "Kierowcę"
    val navController = rememberNavController()

    // 2. Tworzymy "Kontener" i mówimy mu, że zaczynamy od ekranu "login"
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // 3. Definiujemy adres "login"
        composable("login") {
            LoginScreen(
                onNavigateToAdmins = {
                    // Kiedy w LoginScreen klikniemy w baner, mówimy kierowcy:
                    // "Jedź do ekranu kontaktowego z adminami"
                    navController.navigate("admin_contact")
                }
            )
        }

        // 4. Definiujemy adres "admin_contact"
        composable("admin_contact") {
            AdminContactScreen(
                onBackClick = {
                    // Kiedy w AdminContactScreen klikniemy "Cofnij", mówimy kierowcy:
                    // "Zdejmij ten ekran ze stosu, wracamy do poprzedniego"
                    navController.popBackStack()
                }
            )
        }

    }
}