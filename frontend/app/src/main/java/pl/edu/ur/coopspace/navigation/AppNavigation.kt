package pl.edu.ur.coopspace.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.coopspace.administration_module.AdminHomeScreen
import pl.edu.ur.coopspace.administration_module.AdminAnnouncementScreen
import pl.edu.ur.coopspace.administration_module.AdminAddAnnouncementScreen
import pl.edu.ur.coopspace.administration_module.AdminViewAnnouncementHistoryScreen
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
                    navController.popBackStack()
                },
                onLogout = {
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
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_history_announcement") {
            AdminViewAnnouncementHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_announcement_documents") {
            AdminViewAnnouncementDocumentsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_users") {
            AdminUsersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
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
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_service_users") {
            AdminServiceUsersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_reports") {
            AdminReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onNavigateToFinishedReports = {
                    navController.navigate("admin_finished_reports")
                },
                onNavigateToReportsInProgress = {
                    navController.navigate("admin_reports_in_progress")
                }
            )
        }

        composable("admin_reports_in_progress") {
            AdminReportsInProgressScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_finished_reports") {
            AdminFinishedReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_payment_settings") {
            AdminPaymentSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_raports") {
            AdminRaportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                },
                onNavigateToServiceReportsRaport = {
                    navController.navigate("admin_raport_service_reports")
                },
                onNavigateToFinishedReports = {
                    navController.navigate("admin_finished_reports")
                },
                onNavigateToStatisticRaport = {
                    navController.navigate("admin_generate_statistic_raport")
                }
            )
        }

        composable("admin_generate_statistic_raport") {
            AdminGenerateStatisticRaportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_raport_service_reports") {
            AdminRaportOfServiceReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
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
                    // Tu w przyszłości otworzymy formularz nowego zgłoszenia
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("resident_tickets") { inclusive = true }
                    }
                }
            )
        }

        composable("service_tickets") {
            ServiceTicketsScreen(
                onTicketClick = { ticketId ->
                    // Szczegóły dla konserwatora
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("service_tickets") { inclusive = true }
                    }
                }
            )
        }
    }
}
