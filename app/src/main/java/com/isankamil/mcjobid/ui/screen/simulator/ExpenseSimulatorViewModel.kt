package com.isankamil.mcjobid.ui.screen.simulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.UserProfileRepository
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.util.Formatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SimulationExpenseItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // "Transportasi", "MUA & Attire", "Asisten & Crew", "Akomodasi", "Lainnya"
    val amount: Long
)

@HiltViewModel
class ExpenseSimulatorViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _jobTitle = MutableStateFlow("")
    val jobTitle: StateFlow<String> = _jobTitle.asStateFlow()

    private val _grossFee = MutableStateFlow(0L)
    val grossFee: StateFlow<Long> = _grossFee.asStateFlow()

    private val _expenseItems = MutableStateFlow<List<SimulationExpenseItem>>(emptyList())
    val expenseItems: StateFlow<List<SimulationExpenseItem>> = _expenseItems.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            _userProfile.value = userProfileRepository.getUserProfile()
        }
    }

    val totalExpenses: StateFlow<Long> = _expenseItems.map { items ->
        items.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val netProfit: StateFlow<Long> = combine(_grossFee, totalExpenses) { fee, exp ->
        fee - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val profitMarginPercentage: StateFlow<Double> = combine(_grossFee, netProfit) { fee, profit ->
        if (fee > 0) (profit.toDouble() / fee.toDouble()) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val recommendedMinFee: StateFlow<Long> = totalExpenses.map { exp ->
        // Rekomendasi tarif agar profit margin minimal 70% (Fee = Expenses / 0.3)
        if (exp > 0) (exp / 0.3).toLong() else 0L
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun updateJobTitle(title: String) {
        _jobTitle.value = title
    }

    fun updateGrossFee(fee: Long) {
        _grossFee.value = fee
    }

    fun addExpenseItem(name: String, category: String, amount: Long) {
        if (name.isBlank() || amount <= 0) return
        _expenseItems.value = _expenseItems.value + SimulationExpenseItem(
            name = name.trim(),
            category = category,
            amount = amount
        )
    }

    fun removeExpenseItem(id: String) {
        _expenseItems.value = _expenseItems.value.filter { it.id != id }
    }

    fun resetSimulation() {
        _jobTitle.value = ""
        _grossFee.value = 0L
        _expenseItems.value = emptyList()
    }

    fun generateSimulationReportText(): String {
        val title = _jobTitle.value.ifBlank { "Simulasi Agenda MC" }
        val feeStr = Formatter.formatCurrency(_grossFee.value)
        val expStr = Formatter.formatCurrency(totalExpenses.value)
        val profitStr = Formatter.formatCurrency(netProfit.value)
        val margin = String.format("%.1f", profitMarginPercentage.value)

        val itemsDetail = if (_expenseItems.value.isEmpty()) {
            "  (Belum ada rincian item pengeluaran)"
        } else {
            _expenseItems.value.joinToString("\n") {
                "  • ${it.name} [${it.category}]: ${Formatter.formatCurrency(it.amount)}"
            }
        }

        val recStr = if (recommendedMinFee.value > 0) Formatter.formatCurrency(recommendedMinFee.value) else "-"

        return """
📊 *SIMULASI ESTIMASI BIAYA & PROFIT JOB MC*
📌 Agenda: $title
💰 Honor Gross: *${feeStr}*

💸 *Rincian Estimasi Biaya ($expStr):*
$itemsDetail

📈 *PROYEKSI LABA BERSIH (NET PROFIT):*
  • Laba Bersih: *${profitStr}*
  • Profit Margin: *${margin}%*

💡 *Rekomendasi Tarif Minimal:* $recStr
---
Dikelola via MCJOB.ID
        """.trimIndent()
    }
}
