package com.isankamil.mcjobid.domain

import com.isankamil.mcjobid.domain.model.*
import com.isankamil.mcjobid.domain.model.Booking.BookingStatus
import com.isankamil.mcjobid.domain.usecase.analytics.GetPerformanceAnalyticsUseCase
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GetPerformanceAnalyticsUseCaseTest {

    private val useCase = GetPerformanceAnalyticsUseCase()
    private val refDate = LocalDate.of(2026, 8, 9)

    @Test
    fun testEmptyDatabaseReturnsSafeDefaultsAndOnboardingInsight() {
        val result = useCase(
            bookings = emptyList(),
            expenses = emptyList(),
            payments = emptyList(),
            clients = emptyList(),
            period = AnalyticsTimePeriod.THIS_MONTH,
            referenceDate = refDate
        )

        assertEquals(0L, result.grossRevenue)
        assertEquals(0L, result.totalExpenses)
        assertEquals(0L, result.netIncome)
        assertEquals(0, result.totalJobs)
        assertEquals(0.0, result.growthPercentage, 0.001)
        assertEquals(0.0, result.profitMargin, 0.001)
        assertTrue(result.insights.isNotEmpty())
    }

    @Test
    fun testNetIncomeAndMarginCalculationForThisMonth() {
        val bookings = listOf(
            Booking(
                id = "b1",
                name = "Wedding Radit & Gita",
                client = "WO Diamond",
                category = "Wedding",
                date = LocalDate.of(2026, 8, 15),
                fee = 6_000_000L,
                dp = 3_000_000L,
                status = BookingStatus.CONFIRMED
            ),
            Booking(
                id = "b2",
                name = "Corporate Gala Night",
                client = "PT Telkom",
                category = "Corporate",
                date = LocalDate.of(2026, 8, 20),
                fee = 10_000_000L,
                dp = 10_000_000L,
                status = BookingStatus.CONFIRMED
            ),
            // Cancelled job should be excluded
            Booking(
                id = "b3",
                name = "Cancelled Event",
                client = "Mr X",
                category = "Birthday",
                date = LocalDate.of(2026, 8, 25),
                fee = 4_000_000L,
                dp = 0L,
                status = BookingStatus.CANCELLED
            ),
            // Last month job (July)
            Booking(
                id = "b_jul",
                name = "July Wedding",
                client = "WO Diamond",
                category = "Wedding",
                date = LocalDate.of(2026, 7, 10),
                fee = 10_000_000L,
                dp = 10_000_000L,
                status = BookingStatus.COMPLETED
            )
        )

        val expenses = listOf(
            Expense(
                id = "e1",
                bookingId = "b1",
                amount = 1_000_000L,
                category = "Transport & Wardrobe",
                date = "2026-08-15"
            ),
            Expense(
                id = "e2",
                bookingId = "b2",
                amount = 1_000_000L,
                category = "MUA",
                date = "2026-08-20"
            )
        )

        val payments = listOf(
            Payment(
                id = "p1",
                bookingId = "b1",
                amount = 3_000_000L,
                paymentDate = "2026-08-01",
                paymentMethod = "Transfer",
                createdAt = LocalDateTime.now()
            ),
            Payment(
                id = "p2",
                bookingId = "b2",
                amount = 10_000_000L,
                paymentDate = "2026-08-05",
                paymentMethod = "Transfer",
                createdAt = LocalDateTime.now()
            )
        )

        val result = useCase(
            bookings = bookings,
            expenses = expenses,
            payments = payments,
            period = AnalyticsTimePeriod.THIS_MONTH,
            referenceDate = refDate
        )

        // Gross = 6M + 10M = 16M
        assertEquals(16_000_000L, result.grossRevenue)
        // Expenses = 1M + 1M = 2M
        assertEquals(2_000_000L, result.totalExpenses)
        // Net Income = 16M - 2M = 14M
        assertEquals(14_000_000L, result.netIncome)
        // Margin = 14M / 16M * 100 = 87.5%
        assertEquals(87.5, result.profitMargin, 0.01)
        // Total Paid = 3M + 10M = 13M
        assertEquals(13_000_000L, result.totalPaid)
        // Outstanding = 3M
        assertEquals(3_000_000L, result.totalOutstanding)
        // Total Jobs = 2
        assertEquals(2, result.totalJobs)
        // Average Fee = 16M / 2 = 8M
        assertEquals(8_000_000L, result.averageFee)
        // Collection rate = 13M / 16M * 100 = 81.25%
        assertEquals(81.25, result.collectionRate, 0.01)
        // Growth % vs July (16M vs 10M) = +60.0%
        assertEquals(60.0, result.growthPercentage, 0.01)

        // Category breakdown
        assertEquals(2, result.categoryBreakdowns.size)
        val corpCat = result.categoryBreakdowns.first { it.category == "Corporate" }
        assertEquals(10_000_000L, corpCat.totalRevenue)
        assertEquals(62.5, corpCat.percentageOfTotal, 0.01)

        val wedCat = result.categoryBreakdowns.first { it.category == "Wedding" }
        assertEquals(6_000_000L, wedCat.totalRevenue)
        assertEquals(37.5, wedCat.percentageOfTotal, 0.01)

        // Smart Insights should contain cash flow warning for b1 outstanding
        val hasCashFlowWarning = result.insights.any { it.type == InsightType.CASH_FLOW_WARNING }
        assertTrue(hasCashFlowWarning)

        // Smart Insights should contain positive growth
        val hasGrowthInsight = result.insights.any { it.type == InsightType.REVENUE_GROWTH }
        assertTrue(hasGrowthInsight)
    }

    @Test
    fun testAllTimeAndRepeatClientAnalytics() {
        val bookings = listOf(
            Booking(
                id = "b1",
                name = "Wedding A",
                client = "WO Diamond",
                category = "Wedding",
                date = LocalDate.of(2026, 6, 10),
                fee = 5_000_000L,
                dp = 5_000_000L,
                status = BookingStatus.COMPLETED
            ),
            Booking(
                id = "b2",
                name = "Wedding B",
                client = "WO Diamond",
                category = "Wedding",
                date = LocalDate.of(2026, 7, 10),
                fee = 6_000_000L,
                dp = 6_000_000L,
                status = BookingStatus.COMPLETED
            ),
            Booking(
                id = "b3",
                name = "Wedding C",
                client = "WO Platinum",
                category = "Wedding",
                date = LocalDate.of(2026, 8, 10),
                fee = 7_000_000L,
                dp = 7_000_000L,
                status = BookingStatus.CONFIRMED
            )
        )

        val result = useCase(
            bookings = bookings,
            expenses = emptyList(),
            payments = emptyList(),
            period = AnalyticsTimePeriod.ALL_TIME,
            referenceDate = refDate
        )

        assertEquals(18_000_000L, result.grossRevenue)
        assertEquals(3, result.totalJobs)
        assertEquals(2, result.topClients.size)

        val topClient = result.topClients.first()
        assertEquals("WO Diamond", topClient.clientName)
        assertEquals(2, topClient.eventCount)
        assertEquals(11_000_000L, topClient.totalRevenue)
        assertTrue(topClient.isRepeatClient)

        // 1 repeat client out of 2 total clients = 50.0%
        assertEquals(50.0, result.repeatClientRate, 0.01)
    }
}
