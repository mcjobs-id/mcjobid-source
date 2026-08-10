package com.isankamil.mcjobid.domain.model

data class FinancialSummary(
    val totalHonor: Long = 0,
    val totalPaid: Long = 0,
    val totalOutstanding: Long = 0,
    val totalExpenses: Long = 0,
    val netIncome: Long = 0,
    val month: String? = null, // Format: "yyyy-MM" or null for all time
    val year: Int? = null // Extracted from month if present
) {
    
    companion object {
        fun empty() = FinancialSummary()
    }
}
