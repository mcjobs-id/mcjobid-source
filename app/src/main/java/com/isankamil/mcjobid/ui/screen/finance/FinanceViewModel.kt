package com.isankamil.mcjobid.ui.screen.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.isankamil.mcjobid.R
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Expense
import com.isankamil.mcjobid.domain.model.FinancialSummary
import com.isankamil.mcjobid.domain.model.Payment
import com.isankamil.mcjobid.domain.usecase.finance.CalculateFinancialSummaryUseCase
import com.isankamil.mcjobid.domain.usecase.finance.ManageExpenseUseCase
import com.isankamil.mcjobid.domain.usecase.finance.ManagePaymentUseCase
import com.isankamil.mcjobid.ui.components.MonthlyData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class FinanceTab {
    PIUTANG,
    UTANG_PENGELUARAN,
    ARUS_KAS
}

enum class PaymentFilter {
    ALL, OUTSTANDING, PARTIAL, PAID
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val bookingRepository: BookingRepository,
    private val managePaymentUseCase: ManagePaymentUseCase,
    private val manageExpenseUseCase: ManageExpenseUseCase,
    private val calculateFinancialSummaryUseCase: CalculateFinancialSummaryUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(FinanceTab.PIUTANG)
    val selectedTab: StateFlow<FinanceTab> = _selectedTab.asStateFlow()

    private val _selectedPaymentFilter = MutableStateFlow(PaymentFilter.ALL)
    val selectedPaymentFilter: StateFlow<PaymentFilter> = _selectedPaymentFilter.asStateFlow()

    private val _selectedExpenseCategory = MutableStateFlow("ALL")
    val selectedExpenseCategory: StateFlow<String> = _selectedExpenseCategory.asStateFlow()

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings.asStateFlow()

    private val _allPayments = MutableStateFlow<List<Payment>>(emptyList())
    val allPayments: StateFlow<List<Payment>> = _allPayments.asStateFlow()

    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val allExpenses: StateFlow<List<Expense>> = _allExpenses.asStateFlow()

    private val _monthlyChartData = MutableStateFlow<List<MonthlyData>>(emptyList())
    val monthlyChartData: StateFlow<List<MonthlyData>> = _monthlyChartData.asStateFlow()

    private val _monthlyExpenseChartData = MutableStateFlow<List<MonthlyData>>(emptyList())
    val monthlyExpenseChartData: StateFlow<List<MonthlyData>> = _monthlyExpenseChartData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            bookingRepository.getAllBookings().collect { bookings ->
                _allBookings.value = bookings
                calculateMonthlyChart(bookings)
            }
        }

        viewModelScope.launch {
            managePaymentUseCase.getAllPayments().collect { payments ->
                _allPayments.value = payments
            }
        }

        viewModelScope.launch {
            manageExpenseUseCase.getAllExpenses().collect { expenses ->
                _allExpenses.value = expenses
                calculateExpenseMonthlyChart(expenses)
            }
        }
    }

    // Canonical calculation engine
    val summary: StateFlow<FinancialSummary> = combine(
        _allBookings,
        _allPayments,
        _allExpenses
    ) { bookings, payments, expenses ->
        calculateFinancialSummaryUseCase(bookings, payments, expenses)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary.empty())

    val filteredBookings: StateFlow<List<Booking>> = combine(
        _allBookings,
        _selectedPaymentFilter
    ) { list, filter ->
        val activeList = list.filter { it.status != Booking.BookingStatus.CANCELLED }
        when (filter) {
            PaymentFilter.ALL -> activeList
            PaymentFilter.OUTSTANDING -> activeList.filter { it.outstanding > 0 }
            PaymentFilter.PARTIAL -> activeList.filter { it.dp > 0 && it.outstanding > 0 }
            PaymentFilter.PAID -> activeList.filter { it.outstanding == 0L && it.fee > 0 }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        _allExpenses,
        _selectedExpenseCategory
    ) { list, category ->
        if (category == "ALL") list else list.filter { it.category.equals(category, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: FinanceTab) {
        _selectedTab.value = tab
    }

    fun setPaymentFilter(filter: PaymentFilter) {
        _selectedPaymentFilter.value = filter
    }

    fun setExpenseCategory(category: String) {
        _selectedExpenseCategory.value = category
    }

    private fun calculateMonthlyChart(bookings: List<Booking>) {
        val currentYear = LocalDate.now().year
        val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val monthlySums = MutableList(12) { 0L }

        bookings.filter { it.date.year == currentYear && it.status != Booking.BookingStatus.CANCELLED }.forEach { b ->
            val monthIdx = b.date.monthValue - 1
            if (monthIdx in 0..11) {
                monthlySums[monthIdx] += b.fee
            }
        }

        _monthlyChartData.value = months.mapIndexed { idx, name ->
            MonthlyData(name, monthlySums[idx])
        }
    }

    private fun calculateExpenseMonthlyChart(expenses: List<Expense>) {
        val currentYear = LocalDate.now().year
        val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val monthlySums = MutableList(12) { 0L }

        expenses.forEach { exp ->
            try {
                val expDate = LocalDate.parse(exp.date)
                if (expDate.year == currentYear) {
                    val monthIdx = expDate.monthValue - 1
                    if (monthIdx in 0..11) {
                        monthlySums[monthIdx] += exp.amount
                    }
                }
            } catch (_: Exception) {}
        }

        _monthlyExpenseChartData.value = months.mapIndexed { idx, name ->
            MonthlyData(name, monthlySums[idx])
        }
    }

    fun addPayment(
        bookingId: String,
        amount: Long,
        paymentDate: String,
        paymentMethod: String,
        notes: String
    ) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            managePaymentUseCase.addPayment(bookingId, amount, paymentDate, paymentMethod, notes)
                .onSuccess {
                    _successMessage.value = "Pembayaran berhasil dicatat."
                }
                .onFailure { error ->
                    _errorMessage.value = context.getString(R.string.err_catat_pembayaran, error.message)
                }
            _isLoading.value = false
        }
    }

    fun deletePayment(payment: Payment) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            managePaymentUseCase.deletePayment(payment)
                .onSuccess {
                    _successMessage.value = "Catatan pembayaran berhasil dihapus."
                }
                .onFailure { error ->
                    _errorMessage.value = context.getString(R.string.err_hapus_pembayaran, error.message)
                }
            _isLoading.value = false
        }
    }

    fun addExpense(
        bookingId: String,
        category: String,
        amount: Long,
        date: String,
        note: String
    ) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            manageExpenseUseCase.addExpense(bookingId, category, amount, date, note)
                .onSuccess {
                    _successMessage.value = "Pengeluaran operasional berhasil disimpan."
                }
                .onFailure { error ->
                    _errorMessage.value = context.getString(R.string.err_simpan_pengeluaran, error.message)
                }
            _isLoading.value = false
        }
    }

    fun deleteExpense(expense: Expense) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            manageExpenseUseCase.deleteExpense(expense)
                .onSuccess {
                    _successMessage.value = "Catatan pengeluaran berhasil dihapus."
                }
                .onFailure { error ->
                    _errorMessage.value = context.getString(R.string.err_hapus_pengeluaran, error.message)
                }
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
