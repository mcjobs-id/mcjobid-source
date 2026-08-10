package com.isankamil.mcjobid.ui.screen.analytics

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ClientRepository
import com.isankamil.mcjobid.data.repository.ExpenseRepository
import com.isankamil.mcjobid.data.repository.PaymentRepository
import com.isankamil.mcjobid.data.repository.ReminderRepository
import com.isankamil.mcjobid.domain.model.AnalyticsTimePeriod
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.PerformanceAnalyticsResult
import com.isankamil.mcjobid.domain.model.Reminder
import com.isankamil.mcjobid.domain.usecase.analytics.GetPerformanceAnalyticsUseCase
import com.isankamil.mcjobid.util.Formatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import javax.inject.Inject

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val data: PerformanceAnalyticsResult) : AnalyticsUiState()
    object Empty : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

/**
 * Wrapper tunggal untuk semua state filter analitika.
 * Diperlukan karena `combine()` Kotlin hanya mendukung maksimal 5 flow —
 * dengan data class ini kita gabungkan 3 state filter menjadi 1 flow.
 */
data class AnalyticsFilter(
    val period: AnalyticsTimePeriod = AnalyticsTimePeriod.THIS_MONTH,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val expenseRepository: ExpenseRepository,
    private val paymentRepository: PaymentRepository,
    private val clientRepository: ClientRepository,
    private val reminderRepository: ReminderRepository,
    private val getPerformanceAnalyticsUseCase: GetPerformanceAnalyticsUseCase
) : ViewModel() {

    // Expose all data for Calendar indicators
    val allBookings: StateFlow<List<Booking>> = bookingRepository.getAllBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<Reminder>> = reminderRepository.getActiveReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _analyticsFilter = MutableStateFlow(AnalyticsFilter())
    val analyticsFilter: StateFlow<AnalyticsFilter> = _analyticsFilter.asStateFlow()

    // Expose selectedTimePeriod untuk backward compat UI
    val selectedTimePeriod: StateFlow<AnalyticsTimePeriod> =
        _analyticsFilter.map { it.period }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsTimePeriod.THIS_MONTH
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Reactive State combining all repositories and active filter
    val uiState: StateFlow<AnalyticsUiState> = combine(
        bookingRepository.getAllBookings(),
        expenseRepository.getAllExpenses(),
        paymentRepository.getAllPayments(),
        clientRepository.getAllClients(),
        _analyticsFilter
    ) { bookings, expenses, payments, clients, filter ->
        try {
            val result = getPerformanceAnalyticsUseCase(
                bookings = bookings,
                expenses = expenses,
                payments = payments,
                clients = clients,
                period = filter.period,
                referenceDate = LocalDate.now(),
                customStartDate = filter.customStartDate,
                customEndDate = filter.customEndDate
            )

            if (result.totalJobs == 0 && result.totalExpenses == 0L && bookings.isEmpty()) {
                AnalyticsUiState.Empty
            } else {
                AnalyticsUiState.Success(result)
            }
        } catch (e: Exception) {
            AnalyticsUiState.Error(e.localizedMessage ?: "Gagal memuat analitika performa")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState.Loading
    )

    /** Pilih periode preset (Bulan Ini, 3 Bulan, dll) — reset custom dates */
    fun setTimePeriod(period: AnalyticsTimePeriod) {
        _analyticsFilter.value = AnalyticsFilter(
            period = period,
            customStartDate = null,
            customEndDate = null
        )
    }

    /**
     * Set filter bulan spesifik.
     * Contoh: user memilih Juli 2025 → data hanya menampilkan job di Juli 2025.
     */
    fun setCustomMonth(yearMonth: YearMonth) {
        _analyticsFilter.value = AnalyticsFilter(
            period = AnalyticsTimePeriod.CUSTOM_MONTH,
            customStartDate = yearMonth.atDay(1),
            customEndDate = yearMonth.atEndOfMonth()
        )
    }

    /**
     * Set filter rentang tanggal spesifik.
     * Contoh: user memilih 1 Jan 2026 – 31 Mar 2026.
     */
    fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        val safeEnd = if (endDate.isBefore(startDate)) startDate else endDate
        _analyticsFilter.value = AnalyticsFilter(
            period = AnalyticsTimePeriod.CUSTOM_RANGE,
            customStartDate = startDate,
            customEndDate = safeEnd
        )
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(300)
            _isRefreshing.value = false
        }
    }

    fun sharePerformanceSummary(context: Context, data: PerformanceAnalyticsResult) {
        val summaryText = buildString {
            appendLine("📊 *LAPORAN PERFORMA BISNIS MC*")
            appendLine("Periode: ${data.displayLabel}")
            appendLine("--------------------------------")
            appendLine("💰 *Net Income (Laba Bersih)*: ${Formatter.formatCurrency(data.netIncome)}")
            appendLine("📈 *Gross Revenue (Omset)*: ${Formatter.formatCurrency(data.grossRevenue)}")
            appendLine("📉 *Total Pengeluaran*: ${Formatter.formatCurrency(data.totalExpenses)}")
            if (data.growthPercentage != 0.0) {
                appendLine("🚀 *Pertumbuhan*: ${if (data.growthPercentage > 0) "+" else ""}${String.format(Locale.US, "%.1f", data.growthPercentage)}%")
            }
            appendLine("✨ *Margin Keuntungan*: ${String.format(Locale.US, "%.1f", data.profitMargin)}%")
            appendLine("--------------------------------")
            appendLine("🎯 *Metrik Acara*:")
            appendLine("• Total Job: ${data.totalJobs} Acara (${data.completedJobs} Selesai, ${data.upcomingJobs} Mendatang)")
            appendLine("• Rata-rata Fee: ${Formatter.formatCurrency(data.averageFee)}")
            appendLine("• Terbayar (DP/Lunas): ${Formatter.formatCurrency(data.totalPaid)}")
            if (data.totalOutstanding > 0) {
                appendLine("• Sisa Piutang: ${Formatter.formatCurrency(data.totalOutstanding)}")
            }
            appendLine("• Collection Rate: ${String.format(Locale.US, "%.1f", data.collectionRate)}%")

            if (data.categoryBreakdowns.isNotEmpty()) {
                appendLine("--------------------------------")
                appendLine("🎪 *Kategori Acara Teratas*:")
                data.categoryBreakdowns.take(3).forEach { cat ->
                    appendLine("• ${cat.category}: ${cat.totalEvents} Event (${String.format(Locale.US, "%.1f", cat.percentageOfTotal)}%) - Avg ${Formatter.formatCurrency(cat.averageFee)}")
                }
            }

            if (data.topClients.isNotEmpty()) {
                appendLine("--------------------------------")
                appendLine("🤝 *Top Partner / Klien*:")
                data.topClients.take(3).forEach { client ->
                    appendLine("• ${client.clientName}: ${client.eventCount} Event (${Formatter.formatCurrency(client.totalRevenue)})")
                }
            }

            appendLine("--------------------------------")
            appendLine("Dihasilkan otomatis oleh *mcjob.id* Hub Bisnis MC Professional 🎙️")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summaryText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Laporan Performa")
        context.startActivity(shareIntent)
    }
}
