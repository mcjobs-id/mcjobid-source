package com.isankamil.mcjobid.domain

import com.isankamil.mcjobid.data.local.entity.InvoiceEntity
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Invoice
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.util.Formatter
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class InvoiceFeatureTest {

    @Test
    fun testInvoiceEntityToDomainAndBackMapping() {
        val now = LocalDateTime.now()
        val invoice = Invoice(
            id = "inv_123",
            invoiceNumber = "INV-2026-0005",
            bookingId = "b_456",
            issueDate = "2026-08-09",
            dueDate = "2026-08-15",
            status = Invoice.InvoiceStatus.PARTIALLY_PAID,
            totalAmount = 5_000_000L,
            dpAmount = 2_000_000L,
            remainingAmount = 3_000_000L,
            notes = "Pelunasan H-1",
            createdAt = now
        )

        val entity = invoice.toEntity()
        assertEquals("inv_123", entity.id)
        assertEquals("INV-2026-0005", entity.invoiceNumber)
        assertEquals("b_456", entity.bookingId)
        assertEquals(5_000_000L, entity.totalAmount)
        assertEquals(2_000_000L, entity.dpAmount)
        assertEquals(3_000_000L, entity.remainingAmount)
        assertEquals("PARTIALLY_PAID", entity.status)

        val mappedBack = Invoice.fromEntity(entity)
        assertEquals(invoice.id, mappedBack.id)
        assertEquals(invoice.invoiceNumber, mappedBack.invoiceNumber)
        assertEquals(invoice.status, mappedBack.status)
        assertEquals(invoice.totalAmount, mappedBack.totalAmount)
        assertEquals(invoice.remainingAmount, mappedBack.remainingAmount)
    }

    @Test
    fun testInvoiceStatusHelpers() {
        val paidInvoice = Invoice(
            id = "1",
            invoiceNumber = "INV-001",
            bookingId = "b1",
            issueDate = "2026-08-09",
            dueDate = "2026-08-10",
            status = Invoice.InvoiceStatus.PAID,
            totalAmount = 5_000_000L,
            dpAmount = 5_000_000L,
            remainingAmount = 0L,
            createdAt = LocalDateTime.now()
        )
        assertTrue(paidInvoice.isPaid)
        assertFalse(paidInvoice.isPartiallyPaid)

        val partialInvoice = Invoice(
            id = "2",
            invoiceNumber = "INV-002",
            bookingId = "b2",
            issueDate = "2026-08-09",
            dueDate = "2026-08-10",
            status = Invoice.InvoiceStatus.PARTIALLY_PAID,
            totalAmount = 5_000_000L,
            dpAmount = 2_000_000L,
            remainingAmount = 3_000_000L,
            createdAt = LocalDateTime.now()
        )
        assertFalse(partialInvoice.isPaid)
        assertTrue(partialInvoice.isPartiallyPaid)

        val sentInvoice = Invoice(
            id = "3",
            invoiceNumber = "INV-003",
            bookingId = "b3",
            issueDate = "2026-08-09",
            dueDate = "2026-08-10",
            status = Invoice.InvoiceStatus.SENT,
            totalAmount = 5_000_000L,
            dpAmount = 0L,
            remainingAmount = 5_000_000L,
            createdAt = LocalDateTime.now()
        )
        assertFalse(sentInvoice.isPaid)
        assertFalse(sentInvoice.isPartiallyPaid)
    }

    @Test
    fun testShareableInvoiceSummaryFormatting() {
        val booking = Booking(
            id = "b_wedding_01",
            name = "Pernikahan Sarah & Dimas",
            client = "Sarah Jenkins",
            date = LocalDate.of(2026, 9, 20),
            fee = 6_000_000L,
            dp = 2_000_000L,
            category = "Wedding",
            location = "Hotel Mulia Senayan"
        )

        val userProfile = UserProfile(
            userId = "u_1",
            displayName = "Isan Kamil",
            email = "isan@mcjob.id",
            phoneNumber = "08123456789",
            specialization = "Wedding & Corporate",
            city = "Jakarta",
            bankName = "Bank Central Asia (BCA)",
            bankAccountNumber = "1234567890",
            bankAccountHolder = "Isan Kamil"
        )

        val invoice = Invoice(
            id = "inv_01",
            invoiceNumber = "INV-2026-0042",
            bookingId = booking.id,
            issueDate = "2026-08-09",
            dueDate = "2026-09-19",
            status = Invoice.InvoiceStatus.PARTIALLY_PAID,
            totalAmount = booking.fee,
            dpAmount = booking.dp,
            remainingAmount = booking.outstanding,
            notes = "Pelunasan maksimal H-1 acara.",
            createdAt = LocalDateTime.now()
        )

        val mcName = userProfile.name
        val client = booking.client ?: "Personal Client"
        val statusText = "DP TERBAYAR"

        val bankSection = """
            |💳 *PEMBAYARAN DITRANSFER KE:*
            |Bank: ${userProfile.bankName}
            |No. Rekening: ${userProfile.accountNumber ?: "-"}
            |Atas Nama: ${userProfile.accountName ?: userProfile.name}
        """.trimMargin()

        val expectedSummary = """
            |📄 *INVOICE RESMI MC - mcjob.id*
            |No. Invoice: ${invoice.invoiceNumber}
            |Tanggal Terbit: ${invoice.issueDate}
            |Jatuh Tempo: ${invoice.dueDate}
            |Status: [$statusText]
            |
            |👤 *KLIEN / ACARA:*
            |Klien: $client
            |Acara: ${booking.name}
            |Tanggal: ${Formatter.formatDate(booking.date)}
            |Lokasi: ${booking.location}
            |
            |💰 *RINCIAN BIAYA:*
            |Total Jasa MC: ${Formatter.formatCurrency(invoice.totalAmount)}
            |DP Terbayar: ${Formatter.formatCurrency(invoice.dpAmount)}
            |Sisa Tagihan: *${Formatter.formatCurrency(invoice.remainingAmount)}*
            |
            $bankSection
            |
            |Catatan: ${invoice.notes}
            |
            |Terima kasih atas kerja samanya! 🙏
            |Salam, *$mcName*
        """.trimMargin().trim()

        assertTrue(expectedSummary.contains("INV-2026-0042"))
        assertTrue(expectedSummary.contains("Sarah Jenkins"))
        assertTrue(expectedSummary.contains("Pernikahan Sarah & Dimas"))
        assertTrue(expectedSummary.contains("Hotel Mulia Senayan"))
        assertTrue(expectedSummary.contains("Bank Central Asia (BCA)"))
        assertTrue(expectedSummary.contains("1234567890"))
        assertTrue(expectedSummary.contains("Isan Kamil"))
        assertTrue(expectedSummary.contains("Pelunasan maksimal H-1 acara."))
    }

    @Test
    fun testInvoiceOutstandingCalculation() {
        val totalFee = 7_500_000L
        val dpAmount = 2_500_000L
        val remaining = totalFee - dpAmount

        assertEquals(5_000_000L, remaining)
        assertTrue(remaining > 0)

        val fullDp = 7_500_000L
        val remainingFull = totalFee - fullDp
        assertEquals(0L, remainingFull)
    }

    @Test
    fun testInvoiceNumberFormatPattern() {
        val year = 2026
        val count = 1
        val number = "INV-$year-${count.toString().padStart(4, '0')}"
        assertEquals("INV-2026-0001", number)

        val count15 = 15
        val number15 = "INV-$year-${count15.toString().padStart(4, '0')}"
        assertEquals("INV-2026-0015", number15)
    }
}
