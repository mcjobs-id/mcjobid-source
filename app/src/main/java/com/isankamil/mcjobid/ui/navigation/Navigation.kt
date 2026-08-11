package com.isankamil.mcjobid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ReminderRepository
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.ui.screen.analytics.AnalyticsScreen
import com.isankamil.mcjobid.ui.screen.analytics.AnalyticsViewModel
import com.isankamil.mcjobid.ui.screen.auth.AuthViewModel
import com.isankamil.mcjobid.ui.screen.auth.LoginScreen
import com.isankamil.mcjobid.ui.screen.booking.BookingFormViewModel
import com.isankamil.mcjobid.ui.screen.daymode.McDayModeScreen
import com.isankamil.mcjobid.ui.screen.daymode.McDayModeViewModel
import com.isankamil.mcjobid.ui.screen.followup.FollowUpScreen
import com.isankamil.mcjobid.ui.screen.invoice.InvoiceScreen
import com.isankamil.mcjobid.ui.screen.invoice.InvoiceViewModel
import com.isankamil.mcjobid.ui.screen.job.CreateJobScreen
import com.isankamil.mcjobid.ui.screen.job.JobDetailScreen
import com.isankamil.mcjobid.ui.screen.job.JobDetailViewModel
import com.isankamil.mcjobid.ui.screen.main.MainScreen
import com.isankamil.mcjobid.ui.screen.notification.NotificationScreen
import com.isankamil.mcjobid.ui.screen.notification.NotificationSimulationScreen
import com.isankamil.mcjobid.ui.screen.onboarding.OnboardingScreen
import com.isankamil.mcjobid.ui.screen.profile.ProfileScreen
import com.isankamil.mcjobid.ui.screen.profile.ProfileViewModel
import com.isankamil.mcjobid.ui.screen.settings.SettingsScreen
import com.isankamil.mcjobid.ui.screen.settings.SettingsViewModel
import com.isankamil.mcjobid.ui.screen.testimonial.TestimonialScreen
import com.isankamil.mcjobid.ui.screen.testimonial.TestimonialViewModel
import com.isankamil.mcjobid.ui.screen.todo.TodoScreen
import com.isankamil.mcjobid.ui.screen.todo.TodoViewModel
import kotlinx.coroutines.launch

import com.isankamil.mcjobid.ui.screen.pricelist.AddEditRateCardScreen
import com.isankamil.mcjobid.ui.screen.pricelist.AddEditRateCardViewModel
import com.isankamil.mcjobid.ui.screen.pricelist.PriceListScreen
import com.isankamil.mcjobid.ui.screen.pricelist.PriceListViewModel
import com.isankamil.mcjobid.ui.screen.splash.SplashScreen
import com.isankamil.mcjobid.util.Constants

import com.isankamil.mcjobid.ui.screen.wizard.WizardScreen
import com.isankamil.mcjobid.ui.screen.wizard.WizardViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Wizard : Screen("wizard")
    object Main : Screen("main")
    object Settings : Screen("settings")
    object Notification : Screen("notification")
    object NotificationSimulation : Screen("notification_simulation")
    object Profile : Screen("profile")
    object Analytics : Screen("analytics")
    object FollowUp : Screen("follow_up")
    object Testimonial : Screen("testimonial")
    object PriceList : Screen("price_list")
    object AddEditRateCard : Screen("add_edit_rate_card?rateCardId={rateCardId}") {
        fun createRoute(rateCardId: String? = null) =
            if (rateCardId != null) "add_edit_rate_card?rateCardId=$rateCardId" else "add_edit_rate_card"
    }
    object QuickActionSettings : Screen("quick_action_settings")
    object Todo : Screen("todo")

    object JobDetail : Screen("job_detail/{bookingId}") {
        fun createRoute(bookingId: String) = "job_detail/$bookingId"
    }

    object CreateJob : Screen("create_job/{bookingId}?clientName={clientName}&category={category}&jobName={jobName}&fee={fee}&notes={notes}") {
        fun createRoute(
            bookingId: String? = null,
            clientName: String? = null,
            category: String? = null,
            jobName: String? = null,
            fee: Long? = null,
            notes: String? = null
        ): String {
            val encNotes = if (notes != null) java.net.URLEncoder.encode(notes, "UTF-8") else ""
            val encJob = if (jobName != null) java.net.URLEncoder.encode(jobName, "UTF-8") else ""
            val encCat = if (category != null) java.net.URLEncoder.encode(category, "UTF-8") else ""
            val encClient = if (clientName != null) java.net.URLEncoder.encode(clientName, "UTF-8") else ""
            val feeStr = if (fee != null && fee > 0) fee.toString() else ""
            return "create_job/${bookingId ?: "null"}?clientName=$encClient&category=$encCat&jobName=$encJob&fee=$feeStr&notes=$encNotes"
        }
    }

    object Invoice : Screen("invoice/{bookingId}") {
        fun createRoute(bookingId: String? = null) = "invoice/${bookingId ?: "null"}"
    }

    object McDayMode : Screen("mc_day_mode/{bookingId}") {
        fun createRoute(bookingId: String) = "mc_day_mode/$bookingId"
    }

    object ResetPassword : Screen("reset_password?oobCode={oobCode}") {
        fun createRoute(oobCode: String) = "reset_password?oobCode=$oobCode"
    }
}

@Composable
fun McJobIdNavigation(
    navController: NavHostController,
    bookingRepository: BookingRepository,
    reminderRepository: ReminderRepository,
    userProfileRepository: UserProfileRepository,
    initialResetCode: String? = null
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialResetCode) {
        if (!initialResetCode.isNullOrBlank()) {
            navController.navigate(Screen.ResetPassword.createRoute(initialResetCode))
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(navArgument("oobCode") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val oobCode = backStackEntry.arguments?.getString("oobCode") ?: ""
            val viewModel: AuthViewModel = hiltViewModel()
            com.isankamil.mcjobid.ui.screen.auth.ResetPasswordScreen(
                oobCode = oobCode,
                viewModel = viewModel,
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ResetPassword.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val isUserLoggedIn = authViewModel.isUserLoggedIn()
                            val currentUid = if (isUserLoggedIn) userProfileRepository.getCurrentUserId() else ""
                            
                            // Fast local Room DB lookup (~2ms)
                            val localProfile = if (isUserLoggedIn && currentUid.isNotBlank()) {
                                userProfileRepository.getUserProfile(currentUid)
                            } else null

                            // Determine target route immediately from local state
                            val target = when {
                                !isUserLoggedIn -> Screen.Onboarding.route
                                localProfile != null && !localProfile.profileCompleted -> Screen.Wizard.route
                                else -> Screen.Main.route
                            }

                            // Trigger Firestore background refresh asynchronously without blocking splash
                            if (isUserLoggedIn && currentUid.isNotBlank()) {
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    try {
                                        userProfileRepository.fetchProfileFromFirestore(currentUid)
                                    } catch (_: Exception) {}
                                }
                            }

                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                navController.navigate(target) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                navController.navigate(Screen.Onboarding.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            OnboardingScreen(
                onComplete = {
                    scope.launch {
                        viewModel.setOnboardingSeen()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val uid = userProfileRepository.getCurrentUserId()
                        var profile = userProfileRepository.getUserProfile(uid)
                        if (profile == null) {
                            try {
                                profile = userProfileRepository.fetchProfileFromFirestore(uid)
                            } catch (_: Exception) {}
                        }
                        val target = if (profile != null && !profile.profileCompleted) {
                            Screen.Wizard.route
                        } else {
                            Screen.Main.route
                        }
                        scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            navController.navigate(target) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Wizard.route) {
            val viewModel: WizardViewModel = hiltViewModel()
            WizardScreen(
                viewModel = viewModel,
                onWizardComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Wizard.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Wizard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToJobDetail = { id ->
                    navController.navigate(Screen.JobDetail.createRoute(id))
                },
                onNavigateToCreateJob = { id, clientName ->
                    navController.navigate(Screen.CreateJob.createRoute(id, clientName))
                },
                onNavigateToInvoice = { id ->
                    navController.navigate(Screen.Invoice.createRoute(id))
                },
                onNavigateToNotification = {
                    navController.navigate(Screen.Notification.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToPriceList = {
                    navController.navigate(Screen.PriceList.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
                },
                onNavigateToFollowUp = {
                    navController.navigate(Screen.FollowUp.route)
                },
                onNavigateToMcDayMode = { id ->
                    navController.navigate(Screen.McDayMode.createRoute(id))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToQuickActionSettings = {
                    navController.navigate(Screen.QuickActionSettings.route)
                },
                onNavigateToTodo = {
                    navController.navigate(Screen.Todo.route)
                },
                onNavigateToTestimonial = {
                    navController.navigate(Screen.Testimonial.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.JobDetail.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) {
            val viewModel: JobDetailViewModel = hiltViewModel()
            JobDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditJobClick = { id -> navController.navigate(Screen.CreateJob.createRoute(id)) },
                onCreateInvoiceClick = { id -> navController.navigate(Screen.Invoice.createRoute(id)) },
                onMcDayModeClick = { id -> navController.navigate(Screen.McDayMode.createRoute(id)) }
            )
        }

        composable(
            route = Screen.CreateJob.route,
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType; nullable = true },
                navArgument("clientName") { type = NavType.StringType; nullable = true; defaultValue = "" },
                navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = "" },
                navArgument("jobName") { type = NavType.StringType; nullable = true; defaultValue = "" },
                navArgument("fee") { type = NavType.StringType; nullable = true; defaultValue = "" },
                navArgument("notes") { type = NavType.StringType; nullable = true; defaultValue = "" }
            )
        ) {
            val viewModel: BookingFormViewModel = hiltViewModel()
            CreateJobScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onJobSaved = { id ->
                    navController.navigate(Screen.JobDetail.createRoute(id)) {
                        popUpTo(Screen.CreateJob.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Invoice.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType; nullable = true })
        ) {
            val viewModel: InvoiceViewModel = hiltViewModel()
            InvoiceScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.McDayMode.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) {
            val viewModel: McDayModeViewModel = hiltViewModel()
            McDayModeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            val viewModel: AnalyticsViewModel = hiltViewModel()
            AnalyticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onCreateJobClick = { navController.navigate(Screen.CreateJob.createRoute(null)) },
                onFollowUpClick = { navController.navigate(Screen.FollowUp.route) },
                onFinanceClick = { 
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.FollowUp.route) {
            FollowUpScreen(
                onBack = { navController.popBackStack() },
                onBookingClick = { id -> navController.navigate(Screen.JobDetail.createRoute(id)) },
                onCreateJob = { navController.navigate(Screen.CreateJob.createRoute(null)) }
            )
        }

        composable(Screen.Notification.route) {
            NotificationScreen(
                onBack = { navController.popBackStack() },
                onCreateJob = { navController.navigate(Screen.CreateJob.createRoute(null)) },
                onOpenSimulation = { navController.navigate(Screen.NotificationSimulation.route) }
            )
        }

        composable(Screen.NotificationSimulation.route) {
            NotificationSimulationScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            val isAccountDeleted by viewModel.isAccountDeleted.collectAsState()

            val navigateToLogin = remember {
                {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            LaunchedEffect(isAccountDeleted) {
                if (isAccountDeleted) {
                    navigateToLogin()
                }
            }

            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToWizard = { navController.navigate(Screen.Wizard.route) },
                onAccountDeleted = navigateToLogin
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToQuickActionSettings = { navController.navigate(Screen.QuickActionSettings.route) },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.QuickActionSettings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            com.isankamil.mcjobid.ui.screen.settings.QuickActionSettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Todo.route) {
            val viewModel: TodoViewModel = hiltViewModel()
            TodoScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Testimonial.route) {
            val viewModel: TestimonialViewModel = hiltViewModel()
            TestimonialScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PriceList.route) {
            val viewModel: PriceListViewModel = hiltViewModel()
            PriceListScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onAddNewRateCard = {
                    navController.navigate(Screen.AddEditRateCard.createRoute(null))
                },
                onEditRateCard = { rateCardId ->
                    navController.navigate(Screen.AddEditRateCard.createRoute(rateCardId))
                },
                onUseRateCardForJob = { rateCard ->
                    val inclusionsSummary = if (rateCard.inclusions.isNotEmpty()) "Fasilitas Paket:\n" + rateCard.inclusions.joinToString("\n") { "• $it" } else ""
                    navController.navigate(
                        Screen.CreateJob.createRoute(
                            bookingId = null,
                            clientName = null,
                            category = rateCard.category,
                            jobName = rateCard.title,
                            fee = rateCard.price,
                            notes = inclusionsSummary
                        )
                    )
                }
            )
        }

        composable(
            route = Screen.AddEditRateCard.route,
            arguments = listOf(
                navArgument("rateCardId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: AddEditRateCardViewModel = hiltViewModel()
            AddEditRateCardScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
    }
}
