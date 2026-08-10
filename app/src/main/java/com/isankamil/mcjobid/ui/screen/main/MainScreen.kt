package com.isankamil.mcjobid.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.isankamil.mcjobid.ui.components.BottomNavBar
import com.isankamil.mcjobid.ui.components.GlobalSearchDialog
import com.isankamil.mcjobid.ui.components.feedback.MCJobSnackbarHost
import com.isankamil.mcjobid.ui.screen.agenda.AgendaScreen
import com.isankamil.mcjobid.ui.screen.agenda.AgendaViewModel
import com.isankamil.mcjobid.ui.screen.client.ClientScreen
import com.isankamil.mcjobid.ui.screen.client.ClientViewModel
import com.isankamil.mcjobid.ui.screen.finance.FinanceScreen
import com.isankamil.mcjobid.ui.screen.finance.FinanceViewModel
import com.isankamil.mcjobid.ui.screen.home.HomeScreen
import com.isankamil.mcjobid.ui.screen.home.HomeViewModel
import com.isankamil.mcjobid.ui.screen.more.MoreScreen

@Composable
fun MainScreen(
    onNavigateToJobDetail: (String) -> Unit,
    onNavigateToCreateJob: (String?, String?) -> Unit,
    onNavigateToInvoice: (String?) -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPriceList: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToFollowUp: () -> Unit,
    onNavigateToMcDayMode: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToQuickActionSettings: () -> Unit,
    onNavigateToTodo: () -> Unit = {},
    onNavigateToTestimonial: () -> Unit,
    onLogout: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf("home") }
    var showGlobalSearch by remember { mutableStateOf(false) }

    val homeViewModel: HomeViewModel = hiltViewModel()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { MCJobSnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentTab,
                onTabSelected = { tab -> currentTab = tab }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    "home" -> HomeScreen(
                        viewModel = homeViewModel,
                        onBookingClick = onNavigateToJobDetail,
                        onAddJobClick = { onNavigateToCreateJob(null, null) },
                        onAddClientClick = { currentTab = "clients" },
                        onCreateInvoiceClick = { onNavigateToInvoice(null) },
                        onAddPaymentClick = { currentTab = "finance" },
                        onAddExpenseClick = { currentTab = "finance" },
                        onAddReminderClick = onNavigateToNotification,
                        onNotificationClick = onNavigateToNotification,
                        onProfileClick = onNavigateToProfile,
                        onSearchClick = { showGlobalSearch = true },
                        onMcDayModeClick = onNavigateToMcDayMode,
                        onTestimonialClick = onNavigateToTestimonial,
                        onPriceListClick = onNavigateToPriceList,
                        onSimulatorClick = onNavigateToSimulator,
                        onAnalyticsClick = onNavigateToAnalytics,
                        onSettingsClick = onNavigateToSettings,
                        onQuickActionSettingsClick = onNavigateToQuickActionSettings,
                        onTodoClick = onNavigateToTodo
                    )
                    "agenda" -> {
                        val agendaViewModel: AgendaViewModel = hiltViewModel()
                        AgendaScreen(
                            viewModel = agendaViewModel,
                            onBookingClick = onNavigateToJobDetail,
                            onAddJobClick = { onNavigateToCreateJob(null, null) }
                        )
                    }
                    "clients" -> {
                        val clientViewModel: ClientViewModel = hiltViewModel()
                        ClientScreen(
                            viewModel = clientViewModel,
                            onBookingClick = onNavigateToJobDetail,
                            onAddJobForClientClick = { clientName ->
                                onNavigateToCreateJob(null, clientName)
                            }
                        )
                    }
                    "finance" -> {
                        val financeViewModel: FinanceViewModel = hiltViewModel()
                        FinanceScreen(
                            viewModel = financeViewModel,
                            onBookingClick = onNavigateToJobDetail
                        )
                    }
                    "more" -> MoreScreen(
                        userProfile = homeViewModel.userProfile.collectAsState().value,
                        isSynced = homeViewModel.isSynced.collectAsState().value,
                        isSyncing = homeViewModel.isSyncing.collectAsState().value,
                        onProfileClick = onNavigateToProfile,
                        onPriceListClick = onNavigateToPriceList,
                        onSimulatorClick = onNavigateToSimulator,
                        onInvoiceClick = { onNavigateToInvoice(null) },
                        onNotificationClick = onNavigateToNotification,
                        onAnalyticsClick = onNavigateToAnalytics,
                        onFollowUpClick = onNavigateToFollowUp,
                        onTodoClick = onNavigateToTodo,
                        onQuickActionSettingsClick = onNavigateToQuickActionSettings,
                        onSettingsClick = onNavigateToSettings,
                        onLogoutClick = onLogout
                    )
                }
            }
        }
    }

    if (showGlobalSearch) {
        val agendaVM: AgendaViewModel = hiltViewModel()
        val clientVM: ClientViewModel = hiltViewModel()
        val searchBookings by agendaVM.allBookings.collectAsState()
        val searchClients by clientVM.clients.collectAsState()

        GlobalSearchDialog(
            bookings = searchBookings,
            clients = searchClients,
            onSelectBooking = onNavigateToJobDetail,
            onSelectClient = { client ->
                currentTab = "clients"
                clientVM.selectClient(client)
            },
            onDismiss = { showGlobalSearch = false }
        )
    }
}
