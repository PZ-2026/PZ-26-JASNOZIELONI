package pl.edu.ur.coopspace.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.core.content.FileProvider
import android.widget.Toast
import pl.edu.ur.coopspace.user_module.UserFinanceApiClient
import pl.edu.ur.coopspace.administration_module.AdminHomeScreen
import pl.edu.ur.coopspace.administration_module.AdminAnnouncementScreen
import pl.edu.ur.coopspace.administration_module.AdminAddAnnouncementScreen
import pl.edu.ur.coopspace.administration_module.AdminViewAnnouncementHistoryScreen
import pl.edu.ur.coopspace.administration_module.AdminAnnouncementDetailsScreen
import pl.edu.ur.coopspace.administration_module.AdminViewAnnouncementDocumentsScreen
import pl.edu.ur.coopspace.administration_module.AdminUsersScreen
import pl.edu.ur.coopspace.administration_module.AdminResidentUsersScreen
import pl.edu.ur.coopspace.administration_module.AdminServiceUsersScreen
import pl.edu.ur.coopspace.administration_module.AdminReportsScreen
import pl.edu.ur.coopspace.administration_module.AdminFinishedReportsScreen
import pl.edu.ur.coopspace.administration_module.AdminReportsInProgressScreen
import pl.edu.ur.coopspace.administration_module.AdminPaymentSettingsScreen
import pl.edu.ur.coopspace.administration_module.AdminRaportsScreen
import pl.edu.ur.coopspace.administration_module.AdminRaportOfServiceReportsScreen
import pl.edu.ur.coopspace.administration_module.AdminGenerateStatisticRaportScreen
import pl.edu.ur.coopspace.administration_module.AdminRepairProtocolsScreen
import pl.edu.ur.coopspace.administration_module.AdminIssueDetailsScreen
import pl.edu.ur.coopspace.auth.AuthSessionStore
import pl.edu.ur.coopspace.registration_module.AdminContactScreen
import pl.edu.ur.coopspace.registration_module.LoginScreen
import pl.edu.ur.coopspace.registration_module.UserRole
import pl.edu.ur.coopspace.ticket_module.ResidentTicketsScreen
import pl.edu.ur.coopspace.ticket_module.ServiceTicketsScreen
import pl.edu.ur.coopspace.user_module.UserHomeScreen
import pl.edu.ur.coopspace.user_module.UserCommunicationScreen
import pl.edu.ur.coopspace.user_module.UserTicketsMenuScreen
import pl.edu.ur.coopspace.user_module.UserFinancesScreen
import pl.edu.ur.coopspace.user_module.UserAnnouncementHistoryScreen
import pl.edu.ur.coopspace.user_module.UserDocumentsScreen
import pl.edu.ur.coopspace.maintainer_module.MaintainerHomeScreen
import pl.edu.ur.coopspace.maintainer_module.MaintainerCommunicationMenuScreen
import pl.edu.ur.coopspace.maintainer_module.MaintainerAnnouncementHistoryScreen
import pl.edu.ur.coopspace.maintainer_module.MaintainerDocumentsScreen
import pl.edu.ur.coopspace.maintainer_module.MaintainerReportsScreen
import pl.edu.ur.coopspace.maintainer_module.MaintainerFinishedReportsScreen
import pl.edu.ur.coopspace.maintainer_module.MaintainerReportsInProgressScreen


@Composable
fun CoopSpaceApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val startDestination = remember {
        val token = AuthSessionStore.getToken(context)
        val role = AuthSessionStore.getRole(context)

        if (!token.isNullOrBlank() && !AuthSessionStore.isTokenValid(token)) {
            AuthSessionStore.clearSession(context)
            "login"
        } else {
            resolveStartDestination(token = token, role = role)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                        UserRole.MIESZKANIEC -> navController.navigate("user_home") {
                            popUpTo("login") { inclusive = true }
                        }
                        UserRole.KONSERWATOR -> navController.navigate("maintainer_home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("admin_contact") {
            AdminContactScreen(
                onBackClick = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }
        
        composable("admin_home") {
            AdminHomeScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onNavigateToAnnouncements = {
                    navController.navigate("admin_announcements")
                },
                onNavigateToUsers = {
                    navController.navigate("admin_users")
                },
                onNavigateToReports = {
                    navController.navigate("admin_reports")
                },
                onNavigateToPaymentSettings = {
                    navController.navigate("admin_payment_settings")
                },
                onNavigateToRaports = {
                    navController.navigate("admin_raports")
                }
            )
        }

        composable("admin_announcements") {
            AdminAnnouncementScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true } // Or popUpTo("login") depending on architecture
                    }
                },
                onNavigateToAddAnnouncement = {
                    navController.navigate("admin_add_announcement")
                },
                onNavigateToHistory = {
                    navController.navigate("admin_history_announcement")
                },
                onNavigateToDocuments = {
                    navController.navigate("admin_announcement_documents")
                }
            )
        }

        composable("admin_add_announcement") {
            AdminAddAnnouncementScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_history_announcement") {
            AdminViewAnnouncementHistoryScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onAnnouncementClick = { id ->
                    navController.navigate("admin_announcement_details/$id")
                }
            )
        }

        composable(
            "admin_announcement_details/{announcementId}",
            arguments = listOf(androidx.navigation.navArgument("announcementId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val announcementId = backStackEntry.arguments?.getInt("announcementId") ?: 1
            AdminAnnouncementDetailsScreen(
                announcementId = announcementId,
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_announcement_documents") {
            AdminViewAnnouncementDocumentsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_users") {
            AdminUsersScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onNavigateToResidents = {
                    navController.navigate("admin_resident_users")
                },
                onNavigateToServiceUsers = {
                    navController.navigate("admin_service_users")
                }
            )
        }

        composable("admin_resident_users") {
            AdminResidentUsersScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_service_users") {
            AdminServiceUsersScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_reports") {
            AdminReportsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onNavigateToFinishedReports = {
                    navController.navigate("admin_finished_reports")
                },
                onNavigateToReportsInProgress = {
                    navController.navigate("admin_reports_in_progress")
                },
                onNavigateToNewReport = {
                    navController.navigate("admin_new_report")
                }
            )
        }

        composable("admin_new_report") {
            pl.edu.ur.coopspace.administration_module.AdminNewReportScreen(
                onBackClick = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }

        composable("admin_reports_in_progress") {
            AdminReportsInProgressScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onTicketClick = { ticketId ->
                    navController.navigate("admin_issue_details/$ticketId")
                }
            )
        }

        composable("admin_finished_reports") {
            AdminFinishedReportsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onTicketClick = { ticketId ->
                    navController.navigate("admin_issue_details/$ticketId")
                }
            )
        }

        composable(
            "admin_issue_details/{ticketId}",
            arguments = listOf(androidx.navigation.navArgument("ticketId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: 1
            AdminIssueDetailsScreen(
                ticketId = ticketId,
                onBackClick = { navController.safePopBackOrFinish(context) },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_payment_settings") {
            AdminPaymentSettingsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_raports") {
            AdminRaportsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onNavigateToServiceReportsRaport = {
                    navController.navigate("admin_raport_service_reports")
                },
                onNavigateToRepairProtocols = {
                    navController.navigate("admin_repair_protocols")
                },
                onNavigateToStatisticRaport = {
                    navController.navigate("admin_generate_statistic_raport")
                }
            )
        }

        composable("admin_repair_protocols") {
            AdminRepairProtocolsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_generate_statistic_raport") {
            AdminGenerateStatisticRaportScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_raport_service_reports") {
            AdminRaportOfServiceReportsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "resident_tickets/{type}",
            arguments = listOf(androidx.navigation.navArgument("type") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val ticketType = backStackEntry.arguments?.getString("type") ?: "CURRENT"
            ResidentTicketsScreen(
                ticketType = ticketType,
                onTicketClick = { ticketId ->
                    navController.navigate("resident_ticket_details/$ticketId")
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                }
            )
        }

        composable("resident_new_ticket") {
            pl.edu.ur.coopspace.ticket_module.ResidentNewTicketScreen(
                onBackClick = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }

        composable(
            "resident_ticket_details/{ticketId}",
            arguments = listOf(androidx.navigation.navArgument("ticketId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: 1
            pl.edu.ur.coopspace.ticket_module.ResidentTicketDetailsScreen(
                ticketId = ticketId.toString(),
                onBackClick = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }

        composable("service_tickets") {
            ServiceTicketsScreen(
                onTicketClick = { ticketId ->
                    navController.navigate("service_ticket_details/$ticketId")
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("service_tickets") { inclusive = true }
                    }
                }
            )
        }

        composable("maintainer_home") {
            MaintainerHomeScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("maintainer_home") { inclusive = true }
                    }
                },
                onNavigateToReports = {
                    navController.navigate("maintainer_reports")
                },
                onNavigateToCommunication = {
                    navController.navigate("maintainer_communication_menu")
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }

        composable("maintainer_reports") {
            MaintainerReportsScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("maintainer_home") { inclusive = true }
                    }
                },
                onNavigateToCompleted = {
                    navController.navigate("maintainer_finished_reports")
                },
                onNavigateToCurrent = {
                    navController.navigate("maintainer_reports_in_progress")
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }

        composable("maintainer_finished_reports") {
            MaintainerFinishedReportsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("maintainer_home") { inclusive = true }
                    }
                },
                onTicketClick = { id ->
                    navController.navigate("service_ticket_details/$id")
                }
            )
        }

        composable("maintainer_reports_in_progress") {
            MaintainerReportsInProgressScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("maintainer_home") { inclusive = true }
                    }
                },
                onTicketClick = { id ->
                    navController.navigate("service_ticket_details/$id")
                }
            )
        }

        composable("maintainer_communication_menu") {
            MaintainerCommunicationMenuScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("maintainer_home") { inclusive = true }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate("maintainer_announcement_history")
                },
                onNavigateToDocuments = {
                    navController.navigate("maintainer_documents")
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }

        composable("maintainer_announcement_history") {
            MaintainerAnnouncementHistoryScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("maintainer_home") { inclusive = true }
                    }
                },
                onAnnouncementClick = { id ->
                    navController.navigate("admin_announcement_details/$id")
                }
            )
        }

        composable("maintainer_documents") {
            MaintainerDocumentsScreen(
                onNavigateBack = {
                    navController.safePopBackOrFinish(context)
                },
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("maintainer_home") { inclusive = true }
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
                    navController.safePopBackOrFinish(context)
                }
            )
        }

        composable("user_home") {
            UserHomeScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                },
                onNavigateToTickets = {
                    navController.navigate("user_tickets_menu")
                },
                onNavigateToCommunication = {
                    navController.navigate("user_communication")
                },
                onNavigateToFinances = {
                    navController.navigate("user_finances")
                }
            )
        }

        composable("user_tickets_menu") {
            UserTicketsMenuScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                },
                onNavigateToAddTicket = {
                    navController.navigate("resident_new_ticket")
                },
                onNavigateToCurrentTickets = {
                    navController.navigate("resident_tickets/CURRENT")
                },
                onNavigateToFinishedTickets = {
                    navController.navigate("resident_tickets/FINISHED")
                }
            )
        }

        composable("user_communication") {
            UserCommunicationScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                },
                onNavigateToHistory = {
                    navController.navigate("user_announcement_history")
                },
                onNavigateToDocuments = {
                    navController.navigate("user_documents")
                }
            )
        }

        composable("user_finances") {
            UserFinancesScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                },
                onNavigateToHistory = {
                    navController.navigate("user_payment_history")
                },
                onGenerateReport = {
                    coroutineScope.launch {
                        UserFinanceApiClient.downloadReport(context)
                            .onSuccess { file ->
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Otwórz raport"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Nie można otworzyć pliku: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            .onFailure { error ->
                                Toast.makeText(context, "Błąd: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            )
        }

        composable("user_payment_history") {
            pl.edu.ur.coopspace.user_module.UserPaymentHistoryScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                },
                onPaymentClick = { id ->
                    navController.navigate("user_payment_details/$id")
                }
            )
        }

        composable(
            "user_payment_details/{chargeId}",
            arguments = listOf(androidx.navigation.navArgument("chargeId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val chargeId = backStackEntry.arguments?.getInt("chargeId") ?: 0
            pl.edu.ur.coopspace.user_module.UserPaymentDetailsScreen(
                chargeId = chargeId,
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }

        composable("user_announcement_history") {
            UserAnnouncementHistoryScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                },
                onAnnouncementClick = { id ->
                    navController.navigate("admin_announcement_details/$id") // Reusing the same details view
                }
            )
        }

        composable("user_documents") {
            UserDocumentsScreen(
                onLogout = {
                    AuthSessionStore.clearSession(context)
                    navController.navigate("login") {
                        popUpTo("user_home") { inclusive = true }
                    }
                },
                onBack = {
                    navController.safePopBackOrFinish(context)
                }
            )
        }
    }
}

private fun resolveStartDestination(token: String?, role: String?): String {
    if (token.isNullOrBlank()) {
        return "login"
    }

    return when (role?.uppercase()) {
        "ADMIN" -> "admin_home"
        "RESIDENT" -> "user_home"
        "MAINTAINER" -> "maintainer_home"
        else -> "login"
    }
}
