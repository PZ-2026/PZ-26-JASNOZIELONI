package pl.edu.ur.coopspace.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.coopspace.administration_module.AdminHomeScreen
import pl.edu.ur.coopspace.registration_module.AdminContactScreen
import pl.edu.ur.coopspace.registration_module.LoginScreen
import pl.edu.ur.coopspace.registration_module.UserRole
import pl.edu.ur.coopspace.ticket_module.ResidentTicketsScreen
import pl.edu.ur.coopspace.ticket_module.ServiceTicketsScreen

@Composable
fun CoopSpaceApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToAdmins = {
                    navController.navigate("admin_contact")
                },
                onLoginSuccess = { role ->
                    when (role) {
                        UserRole.ADMINISTRATOR -> navController.navigate("admin_home") {
                            popUpTo("login") { inclusive = true }
                        }
                        UserRole.MIESZKANIEC -> navController.navigate("resident_tickets") {
                            popUpTo("login") { inclusive = true }
                        }
                        UserRole.KONSERWATOR -> navController.navigate("service_tickets") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("admin_contact") {
            AdminContactScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("admin_home") {
            AdminHomeScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("resident_tickets") {
            ResidentTicketsScreen(
                onTicketClick = { ticketId ->
                    // Tu w przyszłości przejdziemy do szczegółów
                },
                onAddNewTicketClick = {
                    navController.navigate("resident_new_ticket")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("resident_tickets") { inclusive = true }
                    }
                }
            )
        }

        composable("resident_new_ticket") {
            pl.edu.ur.coopspace.ticket_module.ResidentNewTicketScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("service_tickets") {
            ServiceTicketsScreen(
                onTicketClick = { ticketId ->
                    navController.navigate("service_ticket_details/$ticketId")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("service_tickets") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "service_ticket_details/{ticketId}",
            arguments = listOf(androidx.navigation.navArgument("ticketId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: 1
            pl.edu.ur.coopspace.ticket_module.ServiceTicketDetailsScreen(
                ticketId = ticketId.toString(),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
