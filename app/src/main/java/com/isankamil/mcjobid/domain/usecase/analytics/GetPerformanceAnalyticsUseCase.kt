package com.isankamil.mcjobid.domain.usecase.analytics

import com.isankamil.mcjobid.domain.model.*
import com.isankamil.mcjobid.util.Formatter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class GetPerformanceAnalyticsUseCase @Inject constructor() {

    operator fun invoke(
        bookings: List<Booking>,
        expenses: List<Expense>,
        payments: List<Payment>,
        clients: List<Client> = emptyList(),
        period: AnalyticsTimePeriod = AnalyticsTimePeriod.THIS_MONTH,
        referenceDate: LocalDate = LocalDate.now(),
        customStartDate: LocalDate? = null,
        customEndDate: LocalDate? = null
    ): PerformanceAnalyticsResult {
        val allActiveBookings = bookings.filter { it.status != Booking.BookingStatus.CANCELLED }

        // 1. Filter bookings and expenses by period
        val (filteredBookings, filteredExpenses, prevRevenue, customLabel) = filterDataByPeriod(
            allActiveBookings = allActiveBookings,
            allExpenses = expenses,
            period = period,
            referenceDate = referenceDate,
            customStartDate = customStartDate,
            customEndDate = customEndDate
        )

        // If no bookings exist at all in current filter
        if (filteredBookings.isEmpty() && filteredExpenses.isEmpty() && allActiveBookings.isEmpty()) {
            return PerformanceAnalyticsResult(
                timePeriod = period,
                customLabel = customLabel,
                insights = listOf(
                    SmartInsight(
                        id = "empty_state_insight",
                        type = InsightType.POSITIVE_MILESTONE,
                        title = "Mulai Analisis Bisnis MC Anda",
                        description = "Catat job pertama Anda untuk membuka wawasan performa pendapatan, arus kas, dan profitabilitas.",
                        actionLabel = "Catat Job Baru",
                        actionRoute = "create_job"
                    )
                )
            )
        }

        // 2. Key calculations
        val grossRevenue = filteredBookings.sumOf { it.fee }
        val totalExpenses = filteredExpenses.sumOf { it.amount }
        val netIncome = grossRevenue - totalExpenses

        val paymentsByJob = payments.groupBy { it.bookingId }
        var totalPaid = 0L
        var totalOutstanding = 0L

        filteredBookings.forEach { booking ->
            val jobPayments = paymentsByJob[booking.id]?.sumOf { it.amount } ?: booking.dp
            val effectivePaid = minOf(booking.fee, maxOf(booking.dp, jobPayments))
            totalPaid += effectivePaid
            totalOutstanding += maxOf(0L, booking.fee - effectivePaid)
        }

        val growthPercentage = if (prevRevenue > 0) {
            ((grossRevenue - prevRevenue).toDouble() / prevRevenue.toDouble()) * 100.0
        } else {
            0.0
        }

        val profitMargin = if (grossRevenue > 0) {
            (netIncome.toDouble() / grossRevenue.toDouble()) * 100.0
        } else {
            0.0
        }

        val averageFee = if (filteredBookings.isNotEmpty()) {
            grossRevenue / filteredBookings.size
        } else {
            0L
        }

        val collectionRate = if (grossRevenue > 0) {
            (totalPaid.toDouble() / grossRevenue.toDouble()) * 100.0
        } else {
            0.0
        }

        val completedJobs = filteredBookings.count {
            it.status == Booking.BookingStatus.COMPLETED || it.date.isBefore(referenceDate)
        }
        val upcomingJobs = filteredBookings.size - completedJobs

        // 3. Category Breakdown
        val categoryBreakdowns = filteredBookings
            .groupBy { it.category.ifBlank { "Lainnya" } }
            .map { (category, list) ->
                val catRevenue = list.sumOf { it.fee }
                val percentage = if (grossRevenue > 0) (catRevenue.toDouble() / grossRevenue.toDouble()) * 100.0 else 0.0
                CategoryBreakdown(
                    category = category,
                    totalEvents = list.size,
                    totalRevenue = catRevenue,
                    percentageOfTotal = percentage,
                    averageFee = if (list.isNotEmpty()) catRevenue / list.size else 0L
                )
            }
            .sortedByDescending { it.totalRevenue }

        // 4. Monthly Trend (last 6 months or 12 months)
        val monthlyTrends = calculateMonthlyTrends(allActiveBookings, expenses, referenceDate)

        // 5. Client & Partner Analytics
        val clientPerformanceList = calculateClientPerformance(filteredBookings)
        val repeatClientsCount = clientPerformanceList.count { it.isRepeatClient }
        val repeatRate = if (clientPerformanceList.isNotEmpty()) {
            (repeatClientsCount.toDouble() / clientPerformanceList.size.toDouble()) * 100.0
        } else {
            0.0
        }

        // 6. Smart Automated Insights Engine
        val smartInsights = generateSmartInsights(
            grossRevenue = grossRevenue,
            totalExpenses = totalExpenses,
            netIncome = netIncome,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding,
            growthPercentage = growthPercentage,
            collectionRate = collectionRate,
            categoryBreakdowns = categoryBreakdowns,
            topClients = clientPerformanceList,
            totalJobs = filteredBookings.size,
            period = period,
            customLabel = customLabel
        )

        return PerformanceAnalyticsResult(
            timePeriod = period,
            customLabel = customLabel,
            grossRevenue = grossRevenue,
            totalExpenses = totalExpenses,
            netIncome = netIncome,
            growthPercentage = growthPercentage,
            profitMargin = profitMargin,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding,
            totalJobs = filteredBookings.size,
            completedJobs = completedJobs,
            upcomingJobs = upcomingJobs,
            averageFee = averageFee,
            collectionRate = collectionRate,
            categoryBreakdowns = categoryBreakdowns,
            monthlyTrends = monthlyTrends,
            topClients = clientPerformanceList.take(5),
            repeatClientRate = repeatRate,
            insights = smartInsights
        )
    }

    private data class PeriodFilterResult(
        val bookings: List<Booking>,
        val expenses: List<Expense>,
        val previousRevenue: Long,
        val customLabel: String?
    )

    private fun filterDataByPeriod(
        allActiveBookings: List<Booking>,
        allExpenses: List<Expense>,
        period: AnalyticsTimePeriod,
        referenceDate: LocalDate,
        customStartDate: LocalDate? = null,
        customEndDate: LocalDate? = null
    ): PeriodFilterResult {
        val currentYearMonth = YearMonth.from(referenceDate)
        val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val displayDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("id-ID"))
        val displayMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID"))

        return when (period) {
            AnalyticsTimePeriod.THIS_MONTH -> {
                val curMonthStr = currentYearMonth.format(monthFormatter)
                val prevMonthStr = currentYearMonth.minusMonths(1).format(monthFormatter)

                val curBookings = allActiveBookings.filter { it.date.format(monthFormatter) == curMonthStr }
                val curExpenses = allExpenses.filter { parseYearMonth(it.date) == curMonthStr }
                val prevBookings = allActiveBookings.filter { it.date.format(monthFormatter) == prevMonthStr }
                val prevRev = prevBookings.sumOf { it.fee }

                PeriodFilterResult(curBookings, curExpenses, prevRev, null)
            }
            AnalyticsTimePeriod.LAST_MONTH -> {
                val targetMonth = currentYearMonth.minusMonths(1)
                val targetMonthStr = targetMonth.format(monthFormatter)
                val prevMonthStr = targetMonth.minusMonths(1).format(monthFormatter)

                val targetBookings = allActiveBookings.filter { it.date.format(monthFormatter) == targetMonthStr }
                val targetExpenses = allExpenses.filter { parseYearMonth(it.date) == targetMonthStr }
                val prevBookings = allActiveBookings.filter { it.date.format(monthFormatter) == prevMonthStr }
                val prevRev = prevBookings.sumOf { it.fee }

                PeriodFilterResult(targetBookings, targetExpenses, prevRev, null)
            }
            AnalyticsTimePeriod.LAST_3_MONTHS -> {
                val startMonth = currentYearMonth.minusMonths(2)
                val startDate = startMonth.atDay(1)
                val endDate = currentYearMonth.atEndOfMonth()

                val prevStartMonth = startMonth.minusMonths(3)
                val prevStartDate = prevStartMonth.atDay(1)
                val prevEndDate = startMonth.minusMonths(1).atEndOfMonth()

                val curBookings = allActiveBookings.filter { !it.date.isBefore(startDate) && !it.date.isAfter(endDate) }
                val curExpenses = allExpenses.filter {
                    val d = tryParseDate(it.date)
                    d != null && !d.isBefore(startDate) && !d.isAfter(endDate)
                }

                val prevBookings = allActiveBookings.filter { !it.date.isBefore(prevStartDate) && !it.date.isAfter(prevEndDate) }
                val prevRev = prevBookings.sumOf { it.fee }

                PeriodFilterResult(curBookings, curExpenses, prevRev, null)
            }
            AnalyticsTimePeriod.THIS_YEAR -> {
                val curYear = referenceDate.year
                val curBookings = allActiveBookings.filter { it.date.year == curYear }
                val curExpenses = allExpenses.filter {
                    val d = tryParseDate(it.date)
                    d?.year == curYear
                }
                val prevBookings = allActiveBookings.filter { it.date.year == curYear - 1 }
                val prevRev = prevBookings.sumOf { it.fee }

                PeriodFilterResult(curBookings, curExpenses, prevRev, null)
            }
            AnalyticsTimePeriod.ALL_TIME -> {
                PeriodFilterResult(allActiveBookings, allExpenses, 0L, null)
            }

            // ─── CUSTOM MONTH: filter satu bulan spesifik ───────────────────────
            AnalyticsTimePeriod.CUSTOM_MONTH -> {
                val targetDate = customStartDate ?: referenceDate
                val targetYM = YearMonth.from(targetDate)
                val targetMonthStr = targetYM.format(monthFormatter)
                val prevMonthStr = targetYM.minusMonths(1).format(monthFormatter)

                val curBookings = allActiveBookings.filter { it.date.format(monthFormatter) == targetMonthStr }
                val curExpenses = allExpenses.filter { parseYearMonth(it.date) == targetMonthStr }
                val prevBookings = allActiveBookings.filter { it.date.format(monthFormatter) == prevMonthStr }
                val prevRev = prevBookings.sumOf { it.fee }

                val label = targetYM.format(displayMonthFormatter)
                    .replaceFirstChar { it.uppercase() }

                PeriodFilterResult(curBookings, curExpenses, prevRev, label)
            }

            // ─── CUSTOM RANGE: filter rentang tanggal spesifik ──────────────────
            AnalyticsTimePeriod.CUSTOM_RANGE -> {
                val startDate = customStartDate ?: referenceDate.withDayOfMonth(1)
                val endDate = customEndDate ?: referenceDate

                val curBookings = allActiveBookings.filter {
                    !it.date.isBefore(startDate) && !it.date.isAfter(endDate)
                }
                val curExpenses = allExpenses.filter {
                    val d = tryParseDate(it.date)
                    d != null && !d.isBefore(startDate) && !d.isAfter(endDate)
                }

                // Previous period: rentang yang sama mundur ke belakang
                val rangeDays = endDate.toEpochDay() - startDate.toEpochDay() + 1
                val prevEndDate = startDate.minusDays(1)
                val prevStartDate = prevEndDate.minusDays(rangeDays - 1)
                val prevBookings = allActiveBookings.filter {
                    !it.date.isBefore(prevStartDate) && !it.date.isAfter(prevEndDate)
                }
                val prevRev = prevBookings.sumOf { it.fee }

                val label = if (startDate.year == endDate.year && startDate.monthValue == endDate.monthValue) {
                    "${startDate.dayOfMonth}–${endDate.format(displayDateFormatter)}"
                } else {
                    "${startDate.format(displayDateFormatter)} – ${endDate.format(displayDateFormatter)}"
                }

                PeriodFilterResult(curBookings, curExpenses, prevRev, label)
            }
        }
    }

    private fun calculateMonthlyTrends(
        allBookings: List<Booking>,
        allExpenses: List<Expense>,
        referenceDate: LocalDate
    ): List<MonthlyPerformanceTrend> {
        val currentYearMonth = YearMonth.from(referenceDate)
        val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val labelFormatter = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("id-ID"))

        // Take last 6 months up to current month
        val months = (5 downTo 0).map { currentYearMonth.minusMonths(it.toLong()) }

        return months.map { ym ->
            val ymStr = ym.format(monthFormatter)
            val monthBookings = allBookings.filter { it.date.format(monthFormatter) == ymStr }
            val monthExpenses = allExpenses.filter { parseYearMonth(it.date) == ymStr }

            val rev = monthBookings.sumOf { it.fee }
            val exp = monthExpenses.sumOf { it.amount }

            MonthlyPerformanceTrend(
                monthLabel = ym.format(labelFormatter),
                yearMonth = ymStr,
                grossRevenue = rev,
                totalExpenses = exp,
                netIncome = rev - exp,
                eventCount = monthBookings.size
            )
        }
    }

    private fun calculateClientPerformance(bookings: List<Booking>): List<ClientPerformance> {
        return bookings
            .filter { !it.client.isNullOrBlank() }
            .groupBy { it.client?.trim().orEmpty() }
            .map { (clientName, list) ->
                ClientPerformance(
                    clientName = clientName,
                    eventCount = list.size,
                    totalRevenue = list.sumOf { it.fee },
                    isRepeatClient = list.size > 1
                )
            }
            .sortedByDescending { it.totalRevenue }
    }

    private fun generateSmartInsights(
        grossRevenue: Long,
        totalExpenses: Long,
        netIncome: Long,
        totalPaid: Long,
        totalOutstanding: Long,
        growthPercentage: Double,
        collectionRate: Double,
        categoryBreakdowns: List<CategoryBreakdown>,
        topClients: List<ClientPerformance>,
        totalJobs: Int,
        period: AnalyticsTimePeriod,
        customLabel: String? = null
    ): List<SmartInsight> {
        val list = mutableListOf<SmartInsight>()
        val periodLabel = customLabel ?: period.label

        if (totalJobs == 0) {
            list.add(
                SmartInsight(
                    id = "ins_no_jobs",
                    type = InsightType.POSITIVE_MILESTONE,
                    title = "Belum Ada Acara di Periode Ini",
                    description = "Tidak ada event tercatat pada periode ${periodLabel.lowercase()}. Catat event baru untuk mengaktifkan analisis.",
                    actionLabel = "Catat Job",
                    actionRoute = "create_job"
                )
            )
            return list
        }

        // 1. Cash Flow Status
        if (totalOutstanding > 0) {
            list.add(
                SmartInsight(
                    id = "ins_outstanding",
                    type = InsightType.CASH_FLOW_WARNING,
                    title = "Sisa Piutang Belum Lunas",
                    description = "Terdapat sisa tagihan piutang sebesar ${Formatter.formatCurrency(totalOutstanding)}. Kirim pengingat follow up untuk mempercepat pencairan kas.",
                    actionLabel = "Follow Up Piutang",
                    actionRoute = "follow_up"
                )
            )
        } else if (totalPaid > 0) {
            list.add(
                SmartInsight(
                    id = "ins_paid_full",
                    type = InsightType.POSITIVE_MILESTONE,
                    title = "Arus Kas 100% Lunas",
                    description = "Semua tagihan honor pada periode ini telah terbayar lunas tanpa piutang tertunda.",
                    actionLabel = null,
                    actionRoute = null
                )
            )
        }

        // 2. Revenue Growth
        if (growthPercentage > 0) {
            list.add(
                SmartInsight(
                    id = "ins_growth_pos",
                    type = InsightType.REVENUE_GROWTH,
                    title = "Pertumbuhan Omset (+${String.format(Locale.US, "%.1f", growthPercentage)}%)",
                    description = "Omset Anda bertumbuh positif dibanding periode sebelumnya. Momentum bisnis MC Anda sedang meningkat!",
                    actionLabel = null,
                    actionRoute = null
                )
            )
        } else if (growthPercentage < 0) {
            list.add(
                SmartInsight(
                    id = "ins_growth_neg",
                    type = InsightType.REVENUE_GROWTH,
                    title = "Omset Turun (${String.format(Locale.US, "%.1f", growthPercentage)}%)",
                    description = "Pendapatan periode ini lebih rendah dari periode sebelumnya. Jaga komunikasi aktif dengan mitra WO dan agensi.",
                    actionLabel = "Catat Job Baru",
                    actionRoute = "create_job"
                )
            )
        }

        // 3. Rate Card / Category Insight
        if (categoryBreakdowns.isNotEmpty()) {
            val topCategory = categoryBreakdowns.first()
            if (topCategory.percentageOfTotal >= 40.0 && categoryBreakdowns.size > 1) {
                list.add(
                    SmartInsight(
                        id = "ins_top_category",
                        type = InsightType.RATE_CARD_OPTIMIZATION,
                        title = "Kategori Dominan: ${topCategory.category}",
                        description = "Segmen ${topCategory.category} menyumbang ${String.format(Locale.US, "%.1f", topCategory.percentageOfTotal)}% (${Formatter.formatCurrency(topCategory.totalRevenue)}) dari total omset Anda.",
                        actionLabel = null,
                        actionRoute = null
                    )
                )
            }

            val highestAvgCategory = categoryBreakdowns.maxByOrNull { it.averageFee }
            if (highestAvgCategory != null && highestAvgCategory.averageFee > 0 && highestAvgCategory.category != topCategory.category) {
                list.add(
                    SmartInsight(
                        id = "ins_high_rate",
                        type = InsightType.RATE_CARD_OPTIMIZATION,
                        title = "Tarif Tertinggi: ${highestAvgCategory.category}",
                        description = "Kategori ${highestAvgCategory.category} memiliki rata-rata tarif tertinggi (${Formatter.formatCurrency(highestAvgCategory.averageFee)}/event). Maksimalkan penawaran pada segmen ini.",
                        actionLabel = null,
                        actionRoute = null
                    )
                )
            }
        }

        // 4. Repeat Client / Top Partner Insight
        val topPartner = topClients.firstOrNull { it.isRepeatClient }
        if (topPartner != null) {
            list.add(
                SmartInsight(
                    id = "ins_top_partner",
                    type = InsightType.TOP_PARTNER,
                    title = "Partner Setia: ${topPartner.clientName}",
                    description = "${topPartner.clientName} telah mempercayakan ${topPartner.eventCount} acara dengan kontribusi ${Formatter.formatCurrency(topPartner.totalRevenue)}. Berikan apresiasi khusus!",
                    actionLabel = null,
                    actionRoute = null
                )
            )
        }

        // 5. Expense Efficiency Insight
        if (grossRevenue > 0 && totalExpenses > 0) {
            val expenseRatio = (totalExpenses.toDouble() / grossRevenue.toDouble()) * 100.0
            if (expenseRatio <= 20.0) {
                list.add(
                    SmartInsight(
                        id = "ins_expense_low",
                        type = InsightType.EXPENSE_EFFICIENCY,
                        title = "Efisiensi Operasional Tinggi",
                        description = "Beban operasional sangat terkendali, hanya ${String.format(Locale.US, "%.1f", expenseRatio)}% dari total pendapatan bruto.",
                        actionLabel = null,
                        actionRoute = null
                    )
                )
            } else if (expenseRatio >= 40.0) {
                list.add(
                    SmartInsight(
                        id = "ins_expense_high",
                        type = InsightType.EXPENSE_EFFICIENCY,
                        title = "Rasio Beban Cukup Tinggi (${String.format(Locale.US, "%.1f", expenseRatio)}%)",
                        description = "Pengeluaran operasional mencapai ${Formatter.formatCurrency(totalExpenses)}. Cek rincian beban untuk menjaga margin keuntungan bersih.",
                        actionLabel = "Kelola Beban",
                        actionRoute = "finance"
                    )
                )
            }
        }

        return list
    }

    private fun parseYearMonth(dateStr: String): String {
        return try {
            if (dateStr.length >= 7) dateStr.substring(0, 7) else ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun tryParseDate(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            try {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e2: Exception) {
                null
            }
        }
    }
}
