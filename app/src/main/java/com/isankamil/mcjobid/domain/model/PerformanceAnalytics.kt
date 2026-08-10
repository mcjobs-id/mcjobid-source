package com.isankamil.mcjobid.domain.model

import java.time.LocalDate

enum class AnalyticsTimePeriod(val label: String) {
    THIS_MONTH("Bulan Ini"),
    LAST_3_MONTHS("3 Bulan"),
    THIS_YEAR("Tahun Ini"),
    LAST_MONTH("Bulan Lalu"),
    ALL_TIME("Semua"),
    CUSTOM_MONTH("Bulan Khusus"),
    CUSTOM_RANGE("Rentang Hari")
}

data class CategoryBreakdown(
    val category: String,
    val totalEvents: Int,
    val totalRevenue: Long,
    val percentageOfTotal: Double,
    val averageFee: Long
)

data class MonthlyPerformanceTrend(
    val monthLabel: String,
    val yearMonth: String, // "yyyy-MM"
    val grossRevenue: Long,
    val totalExpenses: Long,
    val netIncome: Long,
    val eventCount: Int
)

data class ClientPerformance(
    val clientName: String,
    val eventCount: Int,
    val totalRevenue: Long,
    val isRepeatClient: Boolean
)

enum class InsightType {
    CASH_FLOW_WARNING,
    REVENUE_GROWTH,
    RATE_CARD_OPTIMIZATION,
    TOP_PARTNER,
    EXPENSE_EFFICIENCY,
    SEASONAL_PEAK,
    POSITIVE_MILESTONE
}

data class SmartInsight(
    val id: String,
    val type: InsightType,
    val title: String,
    val description: String,
    val actionLabel: String? = null,
    val actionRoute: String? = null // e.g. "follow_up", "create_job", "expense", "finance"
)

data class PerformanceAnalyticsResult(
    val timePeriod: AnalyticsTimePeriod = AnalyticsTimePeriod.THIS_MONTH,
    val customLabel: String? = null, // Label kustom untuk CUSTOM_MONTH / CUSTOM_RANGE
    val grossRevenue: Long = 0L,
    val totalExpenses: Long = 0L,
    val netIncome: Long = 0L,
    val growthPercentage: Double = 0.0,
    val profitMargin: Double = 0.0,
    val totalPaid: Long = 0L,
    val totalOutstanding: Long = 0L,
    val totalJobs: Int = 0,
    val completedJobs: Int = 0,
    val upcomingJobs: Int = 0,
    val averageFee: Long = 0L,
    val collectionRate: Double = 0.0,
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val monthlyTrends: List<MonthlyPerformanceTrend> = emptyList(),
    val topClients: List<ClientPerformance> = emptyList(),
    val repeatClientRate: Double = 0.0,
    val insights: List<SmartInsight> = emptyList()
) {
    /** Label yang ditampilkan di UI — gunakan customLabel jika tersedia, fallback ke timePeriod.label */
    val displayLabel: String get() = customLabel ?: timePeriod.label
}
