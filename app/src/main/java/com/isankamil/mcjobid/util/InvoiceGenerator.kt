package com.isankamil.mcjobid.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.InvoiceTemplate
import com.isankamil.mcjobid.domain.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InvoiceGenerator(private val context: Context) {

    private val pageWidth = 595 // A4 width in points (72 dpi)
    private val pageHeight = 842 // A4 height in points (72 dpi)
    private val margin = 40f
    private val contentWidth = pageWidth - (2 * margin)

    // Paints
    private val brandPaint = Paint().apply {
        color = Color.parseColor("#4F46E5") // Primary Indigo
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val titlePaint = Paint().apply {
        color = Color.parseColor("#0F172A") // Slate 900
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val subtitlePaint = Paint().apply {
        color = Color.parseColor("#64748B") // Slate 500
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }

    private val sectionHeaderPaint = Paint().apply {
        color = Color.parseColor("#4F46E5")
        textSize = 10.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val boldPaint = Paint().apply {
        color = Color.parseColor("#1E293B") // Slate 800
        textSize = 10.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val bodyPaint = Paint().apply {
        color = Color.parseColor("#334155") // Slate 700
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }

    private val smallMutedPaint = Paint().apply {
        color = Color.parseColor("#94A3B8") // Slate 400
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }

    private val tableHeaderPaint = Paint().apply {
        color = Color.WHITE
        textSize = 10.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.parseColor("#E2E8F0") // Slate 200
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    fun generateInvoice(
        booking: Booking,
        userProfile: UserProfile?,
        invoiceNumber: String = "INV-${booking.id}",
        issueDate: String = LocalDate.now().toString(),
        dueDate: String = booking.date.toString(),
        notes: String? = null,
        template: InvoiceTemplate = InvoiceTemplate.MODERN_CORPORATE,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            when (template) {
                InvoiceTemplate.MODERN_CORPORATE -> {
                    generateCorporateLayout(canvas, booking, userProfile, invoiceNumber, issueDate, dueDate, notes)
                }
                InvoiceTemplate.LUXURY_ELEGANT -> {
                    generateLuxuryLayout(canvas, booking, userProfile, invoiceNumber, issueDate, dueDate, notes)
                }
                InvoiceTemplate.MINIMALIST_CREATIVE -> {
                    generateCreativeLayout(canvas, booking, userProfile, invoiceNumber, issueDate, dueDate, notes)
                }
            }

            pdfDocument.finishPage(page)

            val file = savePdfToFile(pdfDocument, invoiceNumber, booking.id)
            pdfDocument.close()

            onSuccess(file)
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun generateSamplePdf(
        template: InvoiceTemplate,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sampleBooking = Booking(
            id = "SAMPLE",
            name = "Wedding Gala & Reception",
            client = "Bpk. Alexander & Ibu Diana",
            date = LocalDate.now().plusDays(14),
            fee = 5000000L,
            dp = 2000000L,
            location = "Grand Ballroom Hotel Indonesia Kempinski",
            address = "Jl. M.H. Thamrin No.1, Jakarta Pusat",
            category = "Wedding",
            mcType = "Couple MC",
            language = "Indonesia & English",
            note = "Syarat & Ketentuan: Pelunasan H-1 acara. Sound system disiapkan WO."
        )

        val sampleProfile = UserProfile(
            userId = "SAMPLE_MC",
            displayName = "MC Professional",
            specialization = "Master of Ceremony Hub",
            city = "Jakarta & Bekasi",
            bankName = "BCA (Bank Central Asia)",
            bankAccountNumber = "8830912839",
            bankAccountHolder = "MC Professional"
        )

        generateInvoice(
            booking = sampleBooking,
            userProfile = sampleProfile,
            invoiceNumber = "INV-2026-SAMPLE",
            issueDate = LocalDate.now().toString(),
            dueDate = LocalDate.now().plusDays(7).toString(),
            notes = sampleBooking.note,
            template = template,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    // ==========================================
    // TEMPLATE 1: MODERN CORPORATE (Clean 2-Col)
    // ==========================================
    private fun generateCorporateLayout(
        canvas: Canvas,
        booking: Booking,
        userProfile: UserProfile?,
        invoiceNumber: String,
        issueDate: String,
        dueDate: String,
        notes: String?
    ) {
        var yPosition = margin + 10f
        yPosition = drawHeaderAndMetadata(canvas, userProfile, invoiceNumber, issueDate, dueDate, yPosition)
        yPosition += 16f

        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
        yPosition += 16f

        yPosition = drawClientAndEventInfo(canvas, booking, yPosition)
        yPosition += 18f

        yPosition = drawItemizedTable(canvas, booking, yPosition)
        yPosition += 16f

        yPosition = drawTotalAndSummary(canvas, booking, yPosition)
        yPosition += 18f

        yPosition = drawPaymentAndBankSection(canvas, booking, userProfile, yPosition)
        yPosition += 16f

        val finalNotes = notes ?: booking.note
        if (!finalNotes.isNullOrBlank()) {
            yPosition = drawNotesSection(canvas, finalNotes, yPosition)
            yPosition += 16f
        }

        drawSignatureSection(canvas, userProfile, yPosition)
        drawFooter(canvas, pageHeight - margin - 15f)
    }

    // ==========================================
    // TEMPLATE 2: LUXURY ELEGANT (Wedding & VIP)
    // ==========================================
    private fun generateLuxuryLayout(
        canvas: Canvas,
        booking: Booking,
        userProfile: UserProfile?,
        invoiceNumber: String,
        issueDate: String,
        dueDate: String,
        notes: String?
    ) {
        val goldColor = Color.parseColor("#B45309")
        val navyColor = Color.parseColor("#0F172A")
        val goldLight = Color.parseColor("#FEF3C7")

        // 1. Ornate Frame Borders
        val outerFramePaint = Paint().apply {
            color = goldColor
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        val innerFramePaint = Paint().apply {
            color = goldColor
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }
        val frameMargin = 20f
        canvas.drawRect(frameMargin, frameMargin, pageWidth - frameMargin, pageHeight - frameMargin, outerFramePaint)
        canvas.drawRect(frameMargin + 4f, frameMargin + 4f, pageWidth - frameMargin - 4f, pageHeight - frameMargin - 4f, innerFramePaint)

        var yPos = margin + 15f

        // 2. Centered Luxury Title Header
        val goldHeaderPaint = Paint().apply {
            color = goldColor
            textSize = 18f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val titleStr = "SURAT PENAWARAN & INVOICE RESMI"
        canvas.drawText(titleStr, (pageWidth - goldHeaderPaint.measureText(titleStr)) / 2f, yPos, goldHeaderPaint)
        yPos += 16f

        val subStr = "CONFIRMED MC BOOKING & HONORARIUM STATEMENT"
        val subHeaderPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText(subStr, (pageWidth - subHeaderPaint.measureText(subStr)) / 2f, yPos, subHeaderPaint)
        yPos += 20f

        // Gold divider line with center diamond flourish
        canvas.drawLine(margin + 50f, yPos, pageWidth - margin - 50f, yPos, outerFramePaint)
        yPos += 20f

        // 3. Salutation & Metadata Row
        val salutationPaint = Paint().apply {
            color = navyColor
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Kepada Yth. Klien Bapak/Ibu ${booking.client ?: "-"}", margin + 10f, yPos, salutationPaint)
        val metaStr = "No: $invoiceNumber  |  Tgl: $issueDate"
        canvas.drawText(metaStr, pageWidth - margin - 10f - bodyPaint.measureText(metaStr), yPos, bodyPaint)
        yPos += 18f

        // 4. Centered Event Highlight Box (Gold Tint)
        val eventBoxRect = RectF(margin + 10f, yPos, pageWidth - margin - 10f, yPos + 64f)
        val eventBoxBg = Paint().apply { color = goldLight }
        val eventBoxBorder = Paint().apply {
            color = goldColor
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(eventBoxRect, 8f, 8f, eventBoxBg)
        canvas.drawRoundRect(eventBoxRect, 8f, 8f, eventBoxBorder)

        var eY = yPos + 18f
        val eventTitle = "ACARA: ${booking.name.uppercase()}"
        canvas.drawText(eventTitle, margin + 20f, eY, salutationPaint)
        eY += 16f

        val formattedDate = try { Formatter.formatDate(booking.date) } catch (e: Exception) { booking.date.toString() }
        val detailStr = "Tanggal: $formattedDate  •  Waktu: ${booking.start ?: "19:00"} - ${booking.end ?: "22:00"} WIB"
        canvas.drawText(detailStr, margin + 20f, eY, bodyPaint)
        eY += 16f

        val venueStr = "Lokasi Venue: ${booking.location ?: "-"}${booking.address?.let { " ($it)" } ?: ""}"
        canvas.drawText(venueStr, margin + 20f, eY, bodyPaint)

        yPos += 78f

        // 5. Itemized Table (Gold Header)
        val tableHeaderRect = RectF(margin + 10f, yPos, pageWidth - margin - 10f, yPos + 24f)
        val goldBgPaint = Paint().apply { color = goldColor }
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, goldBgPaint)

        canvas.drawText("Deskripsi Layanan & Peran MC", margin + 22f, yPos + 16f, tableHeaderPaint)
        val totalHeader = "Total Honor"
        canvas.drawText(totalHeader, pageWidth - margin - 22f - tableHeaderPaint.measureText(totalHeader), yPos + 16f, tableHeaderPaint)
        yPos += 36f

        // Table Rows
        val mcService = "Jasa Professional Master of Ceremony (MC) - ${booking.name}"
        canvas.drawText(mcService, margin + 22f, yPos, boldPaint)
        val feeStr = Formatter.formatCurrency(booking.fee)
        canvas.drawText(feeStr, pageWidth - margin - 22f - boldPaint.measureText(feeStr), yPos, boldPaint)
        yPos += 16f

        val mcDetail = "Spesialisasi: ${booking.category} (${booking.mcType ?: "Single MC"}) | Bahasa: ${booking.language ?: "Indonesia"}"
        canvas.drawText(mcDetail, margin + 22f, yPos, smallMutedPaint)
        yPos += 20f

        if (booking.dp > 0) {
            canvas.drawLine(margin + 20f, yPos - 6f, pageWidth - margin - 20f, yPos - 6f, linePaint)
            val dpLabel = "Uang Muka (DP) Terbayar (Konfirmasi Berhasil)"
            val dpVal = "- ${Formatter.formatCurrency(booking.dp)}"
            val successP = Paint(boldPaint).apply { color = Color.parseColor("#059669") }
            canvas.drawText(dpLabel, margin + 22f, yPos + 8f, bodyPaint)
            canvas.drawText(dpVal, pageWidth - margin - 22f - successP.measureText(dpVal), yPos + 8f, successP)
            yPos += 24f
        }

        canvas.drawLine(margin + 10f, yPos, pageWidth - margin - 10f, yPos, outerFramePaint)
        yPos += 18f

        // 6. Outstanding Summary & Terms Section
        val rightX = pageWidth - margin - 20f
        val outVal = Formatter.formatCurrency(booking.outstanding)
        val luxuryOutPaint = Paint().apply {
            color = goldColor
            textSize = 13f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("SISA PELUNASAN HONOR (H-1 ACARA):", rightX - 280f, yPos, boldPaint)
        canvas.drawText(outVal, rightX - luxuryOutPaint.measureText(outVal), yPos, luxuryOutPaint)
        yPos += 30f

        // Cancellation & Booking Terms Box
        val termsBoxRect = RectF(margin + 10f, yPos, pageWidth - margin - 10f, yPos + 68f)
        val termsBg = Paint().apply { color = Color.parseColor("#F8FAFC") }
        canvas.drawRoundRect(termsBoxRect, 6f, 6f, termsBg)

        canvas.drawText("KEBIJAKAN & SYARAT KETENTUAN MC:", margin + 20f, yPos + 16f, sectionHeaderPaint)
        canvas.drawText("1. Pelunasan sisa honor dilakukan paling lambat H-1 sebelum pelaksanaan acara.", margin + 20f, yPos + 30f, bodyPaint)
        canvas.drawText("2. Pembatalan sepihak oleh klien membuat DP yang telah masuk tidak dapat dikembalikan.", margin + 20f, yPos + 44f, bodyPaint)
        val customN = notes ?: booking.note
        if (!customN.isNullOrBlank()) {
            canvas.drawText("3. Catatan: ${customN.take(75)}", margin + 20f, yPos + 58f, bodyPaint)
        }
        yPos += 80f

        // 7. Bank Info & Verified Stamp Section
        if (userProfile != null && !userProfile.bankName.isNullOrBlank()) {
            canvas.drawText("REKENING RESMI MC:", margin + 20f, yPos, boldPaint)
            canvas.drawText("Bank: ${userProfile.bankName}  |  No: ${userProfile.accountNumber ?: "-"}  |  a.n: ${userProfile.accountName ?: userProfile.name}", margin + 20f, yPos + 14f, bodyPaint)
        }

        // Stamp badge on right
        val stampCenterY = yPos + 10f
        val stampCenterX = pageWidth - margin - 70f
        val stampPaint = Paint().apply {
            color = goldColor
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawCircle(stampCenterX, stampCenterY, 28f, stampPaint)
        val stampTextP = Paint().apply {
            color = goldColor
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val s1 = "OFFICIALLY"
        val s2 = "VERIFIED"
        canvas.drawText(s1, stampCenterX - stampTextP.measureText(s1) / 2f, stampCenterY - 2f, stampTextP)
        canvas.drawText(s2, stampCenterX - stampTextP.measureText(s2) / 2f, stampCenterY + 8f, stampTextP)

        yPos += 50f

        // Signature
        drawSignatureSection(canvas, userProfile, yPos)
        drawFooter(canvas, pageHeight - margin - 25f)
    }

    // ==========================================
    // TEMPLATE 3: MINIMALIST CREATIVE (Gigs/Party)
    // ==========================================
    private fun generateCreativeLayout(
        canvas: Canvas,
        booking: Booking,
        userProfile: UserProfile?,
        invoiceNumber: String,
        issueDate: String,
        dueDate: String,
        notes: String?
    ) {
        val emeraldColor = Color.parseColor("#059669")
        val darkCharcoal = Color.parseColor("#18181B")
        val cardBgColor = Color.parseColor("#F4F4F5")

        // 1. Thick Left Accent Bar
        val accentBarPaint = Paint().apply { color = emeraldColor }
        canvas.drawRect(0f, 0f, 16f, pageHeight.toFloat(), accentBarPaint)

        var yPos = margin + 10f

        // 2. Giant Modern INVOICE Title
        val giantTitlePaint = Paint().apply {
            color = darkCharcoal
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("INVOICE", margin + 10f, yPos + 10f, giantTitlePaint)

        val metaPaint = Paint().apply {
            color = Color.parseColor("#71717A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val rightX = pageWidth - margin
        canvas.drawText("# $invoiceNumber", rightX - metaPaint.measureText("# $invoiceNumber"), yPos, metaPaint)
        canvas.drawText("Tgl: $issueDate", rightX - bodyPaint.measureText("Tgl: $issueDate"), yPos + 14f, bodyPaint)
        canvas.drawText("Jatuh Tempo: $dueDate", rightX - bodyPaint.measureText("Jatuh Tempo: $dueDate"), yPos + 28f, bodyPaint)

        yPos += 48f

        // 3. Compact Info Cards (Grid Blocks)
        val cardWidth = (contentWidth - 10f) / 2f

        // Card Left: Klien
        val cardLeftRect = RectF(margin + 10f, yPos, margin + 10f + cardWidth, yPos + 60f)
        val cardBgPaint = Paint().apply { color = cardBgColor }
        canvas.drawRoundRect(cardLeftRect, 10f, 10f, cardBgPaint)

        val cardLabelP = Paint().apply {
            color = emeraldColor
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("DIBAYAR OLEH (KLIEN):", margin + 20f, yPos + 18f, cardLabelP)
        canvas.drawText(booking.client ?: "Personal Client", margin + 20f, yPos + 34f, boldPaint)
        booking.pic?.let { canvas.drawText("PIC: $it", margin + 20f, yPos + 48f, bodyPaint) }

        // Card Right: Acara
        val cardRightRect = RectF(margin + 20f + cardWidth, yPos, pageWidth - margin, yPos + 60f)
        canvas.drawRoundRect(cardRightRect, 10f, 10f, cardBgPaint)

        val rightCardX = margin + 30f + cardWidth
        canvas.drawText("DETAIL GIGS / ACARA:", rightCardX, yPos + 18f, cardLabelP)
        canvas.drawText(booking.name, rightCardX, yPos + 34f, boldPaint)
        val formattedDate = try { Formatter.formatDate(booking.date) } catch (e: Exception) { booking.date.toString() }
        canvas.drawText("$formattedDate (${booking.start ?: "19:00"} - ${booking.end ?: "22:00"})", rightCardX, yPos + 48f, bodyPaint)

        yPos += 74f

        // 4. Item Block
        canvas.drawText("RINCIAN JASA MC", margin + 10f, yPos, cardLabelP)
        yPos += 14f

        val itemRect = RectF(margin + 10f, yPos, pageWidth - margin, yPos + 40f)
        canvas.drawRoundRect(itemRect, 8f, 8f, cardBgPaint)
        canvas.drawText("Jasa Master of Ceremony - ${booking.category} (${booking.mcType ?: "Single"})", margin + 20f, yPos + 24f, boldPaint)
        val feeStr = Formatter.formatCurrency(booking.fee)
        canvas.drawText(feeStr, pageWidth - margin - 20f - boldPaint.measureText(feeStr), yPos + 24f, boldPaint)

        yPos += 54f

        // 5. Giant Total Honor Banner (Emerald Solid)
        val totalBannerRect = RectF(margin + 10f, yPos, pageWidth - margin, yPos + 54f)
        val emeraldBgPaint = Paint().apply { color = emeraldColor }
        canvas.drawRoundRect(totalBannerRect, 12f, 12f, emeraldBgPaint)

        val bannerLabelP = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bannerValP = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TOTAL SISA PIUTANG HONOR", margin + 24f, yPos + 32f, bannerLabelP)
        val outValStr = Formatter.formatCurrency(booking.outstanding)
        canvas.drawText(outValStr, pageWidth - margin - 24f - bannerValP.measureText(outValStr), yPos + 34f, bannerValP)

        yPos += 70f

        // 6. Direct Transfer & Quick WA Contact Card
        if (userProfile != null && !userProfile.bankName.isNullOrBlank()) {
            val payCardRect = RectF(margin + 10f, yPos, pageWidth - margin, yPos + 50f)
            canvas.drawRoundRect(payCardRect, 8f, 8f, cardBgPaint)

            canvas.drawText("PEMBAYARAN VIA TRANSFER:", margin + 20f, yPos + 18f, cardLabelP)
            canvas.drawText("Bank: ${userProfile.bankName}  |  No. Rek: ${userProfile.accountNumber ?: "-"} (a.n ${userProfile.accountName ?: userProfile.name})", margin + 20f, yPos + 34f, boldPaint)
            yPos += 64f
        }

        val customNotes = notes ?: booking.note
        if (!customNotes.isNullOrBlank()) {
            canvas.drawText("Catatan: ${customNotes.take(90)}", margin + 10f, yPos, bodyPaint)
            yPos += 20f
        }

        // Signature & Footer
        drawSignatureSection(canvas, userProfile, yPos)
        drawFooter(canvas, pageHeight - margin - 15f)
    }

    private fun drawHeaderAndMetadata(
        canvas: Canvas,
        userProfile: UserProfile?,
        invoiceNumber: String,
        issueDate: String,
        dueDate: String,
        y: Float
    ): Float {
        var currentY = y

        // Left side: mcjob.id Branding & MC Profile
        canvas.drawText("mcjob.id", margin, currentY + 4f, brandPaint)
        canvas.drawText("Professional MC Management Hub", margin, currentY + 18f, subtitlePaint)

        val mcName = userProfile?.name ?: "MC Professional"
        canvas.drawText("Penyedia Jasa: $mcName", margin, currentY + 34f, boldPaint)
        
        val contactLine = listOfNotNull(
            userProfile?.specialization,
            userProfile?.city
        ).joinToString(" • ")
        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, margin, currentY + 47f, bodyPaint)
        }

        // Right side: INVOICE Tag & Metadata
        val rightX = pageWidth - margin
        val titleText = "INVOICE"
        val titleWidth = titlePaint.measureText(titleText)
        canvas.drawText(titleText, rightX - titleWidth, currentY + 4f, titlePaint)

        val invText = "No: $invoiceNumber"
        canvas.drawText(invText, rightX - boldPaint.measureText(invText), currentY + 20f, boldPaint)

        val issueText = "Tgl Terbit: $issueDate"
        canvas.drawText(issueText, rightX - bodyPaint.measureText(issueText), currentY + 34f, bodyPaint)

        val dueText = "Jatuh Tempo: $dueDate"
        canvas.drawText(dueText, rightX - bodyPaint.measureText(dueText), currentY + 47f, bodyPaint)

        return currentY + 54f
    }

    private fun drawClientAndEventInfo(canvas: Canvas, booking: Booking, y: Float): Float {
        var currentY = y
        val colWidth = contentWidth / 2f

        // Column 1: Bill To (Client)
        canvas.drawText("DITUJUKAN KEPADA (BILL TO):", margin, currentY, sectionHeaderPaint)
        var col1Y = currentY + 14f

        val clientName = booking.client ?: "Personal Client"
        canvas.drawText(clientName, margin, col1Y, boldPaint)
        col1Y += 14f

        booking.pic?.let { pic ->
            canvas.drawText("PIC / Kontak: $pic", margin, col1Y, bodyPaint)
            col1Y += 14f
        }

        // Column 2: Event Details
        val col2X = margin + colWidth
        canvas.drawText("DETAIL ACARA (EVENT):", col2X, currentY, sectionHeaderPaint)
        var col2Y = currentY + 14f

        canvas.drawText(booking.name, col2X, col2Y, boldPaint)
        col2Y += 14f

        val formattedDate = try {
            Formatter.formatDate(booking.date)
        } catch (e: Exception) {
            booking.date.toString()
        }
        val timeStr = if (!booking.start.isNullOrBlank() && !booking.end.isNullOrBlank()) {
            " • ${booking.start} - ${booking.end}"
        } else ""
        canvas.drawText("Tanggal: $formattedDate$timeStr", col2X, col2Y, bodyPaint)
        col2Y += 14f

        booking.location?.let { loc ->
            val venueText = if (loc.length > 35) loc.take(32) + "..." else loc
            canvas.drawText("Lokasi: $venueText", col2X, col2Y, bodyPaint)
            col2Y += 14f
        }

        return maxOf(col1Y, col2Y)
    }

    private fun drawItemizedTable(canvas: Canvas, booking: Booking, y: Float): Float {
        var currentY = y
        val tableHeaderHeight = 24f
        val rect = RectF(margin, currentY, pageWidth - margin, currentY + tableHeaderHeight)

        // Draw header background (Primary Indigo)
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
        }
        canvas.drawRoundRect(rect, 4f, 4f, headerBgPaint)

        // Draw header text
        canvas.drawText("Deskripsi Layanan", margin + 12f, currentY + 16f, tableHeaderPaint)
        val amountHeader = "Jumlah (Rp)"
        canvas.drawText(amountHeader, pageWidth - margin - tableHeaderPaint.measureText(amountHeader) - 12f, currentY + 16f, tableHeaderPaint)

        currentY += tableHeaderHeight + 14f

        // Item 1: Jasa MC
        val itemName = "Jasa Master of Ceremony (MC) - ${booking.name}"
        val itemFeeStr = Formatter.formatCurrency(booking.fee)
        canvas.drawText(itemName, margin + 12f, currentY, boldPaint)
        canvas.drawText(itemFeeStr, pageWidth - margin - boldPaint.measureText(itemFeeStr) - 12f, currentY, boldPaint)
        currentY += 16f

        val categoryDetail = "Kategori: ${booking.category} | ${booking.mcType ?: "Single MC"}"
        canvas.drawText(categoryDetail, margin + 12f, currentY, smallMutedPaint)
        currentY += 14f

        // Item 2: DP Masuk (if any)
        if (booking.dp > 0) {
            canvas.drawLine(margin + 10f, currentY, pageWidth - margin - 10f, currentY, linePaint)
            currentY += 14f

            val dpLabel = "Pembayaran Uang Muka (DP) Terbayar"
            val dpAmountStr = "- ${Formatter.formatCurrency(booking.dp)}"
            val successPaint = Paint(boldPaint).apply { color = Color.parseColor("#059669") }
            canvas.drawText(dpLabel, margin + 12f, currentY, bodyPaint)
            canvas.drawText(dpAmountStr, pageWidth - margin - successPaint.measureText(dpAmountStr) - 12f, currentY, successPaint)
            currentY += 16f
        }

        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
        return currentY
    }

    private fun drawTotalAndSummary(canvas: Canvas, booking: Booking, y: Float): Float {
        var currentY = y + 10f
        val rightX = pageWidth - margin - 12f

        // Total Fee
        val totalLabel = "Total Honor:"
        val totalValue = Formatter.formatCurrency(booking.fee)
        canvas.drawText(totalLabel, rightX - 220f, currentY, bodyPaint)
        canvas.drawText(totalValue, rightX - boldPaint.measureText(totalValue), currentY, boldPaint)
        currentY += 16f

        // DP
        if (booking.dp > 0) {
            val dpLabel = "Total Terbayar (DP):"
            val dpValue = Formatter.formatCurrency(booking.dp)
            canvas.drawText(dpLabel, rightX - 220f, currentY, bodyPaint)
            canvas.drawText(dpValue, rightX - bodyPaint.measureText(dpValue), currentY, bodyPaint)
            currentY += 16f
        }

        // Outstanding Box
        val boxHeight = 28f
        val boxRect = RectF(rightX - 230f, currentY, pageWidth - margin, currentY + boxHeight)
        val boxBg = Paint().apply {
            color = Color.parseColor("#EEF2FF") // Indigo light 50
        }
        canvas.drawRoundRect(boxRect, 6f, 6f, boxBg)

        val outLabel = "SISA TAGIHAN:"
        val outValue = Formatter.formatCurrency(booking.outstanding)
        val outPaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(outLabel, rightX - 220f, currentY + 18f, outPaint)
        canvas.drawText(outValue, rightX - outPaint.measureText(outValue), currentY + 18f, outPaint)

        return currentY + boxHeight
    }

    private fun drawPaymentAndBankSection(
        canvas: Canvas,
        booking: Booking,
        userProfile: UserProfile?,
        y: Float
    ): Float {
        var currentY = y + 8f

        // Payment status badge
        val statusText = when (booking.paymentStatus) {
            Booking.PaymentStatus.PAID -> "LUNAS"
            Booking.PaymentStatus.PARTIAL -> "DP TERBAYAR (SEBAGIAN)"
            Booking.PaymentStatus.UNPAID -> "BELUM BAYAR"
            Booking.PaymentStatus.OVERDUE -> "JATUH TEMPO"
            Booking.PaymentStatus.TBD -> "MENUNGGU KONFIRMASI"
        }
        val (badgeColor, badgeBg) = when (booking.paymentStatus) {
            Booking.PaymentStatus.PAID -> Color.parseColor("#059669") to Color.parseColor("#ECFDF5")
            Booking.PaymentStatus.PARTIAL -> Color.parseColor("#D97706") to Color.parseColor("#FFFBEB")
            else -> Color.parseColor("#DC2626") to Color.parseColor("#FEF2F2")
        }

        val badgePaint = Paint().apply {
            color = badgeColor
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textWidth = badgePaint.measureText(statusText)
        val badgeRect = RectF(margin, currentY, margin + textWidth + 18f, currentY + 20f)
        val bgPaint = Paint().apply { color = badgeBg }
        canvas.drawRoundRect(badgeRect, 6f, 6f, bgPaint)
        canvas.drawText(statusText, margin + 9f, currentY + 14f, badgePaint)

        currentY += 30f

        // Bank Transfer Box
        if (userProfile != null && !userProfile.bankName.isNullOrBlank()) {
            val bankBoxRect = RectF(margin, currentY, margin + 300f, currentY + 54f)
            val bankBg = Paint().apply { color = Color.parseColor("#F8FAFC") }
            canvas.drawRoundRect(bankBoxRect, 8f, 8f, bankBg)

            canvas.drawText("INFORMASI REKENING PEMBAYARAN:", margin + 10f, currentY + 14f, sectionHeaderPaint)
            canvas.drawText("Bank: ${userProfile.bankName}", margin + 10f, currentY + 28f, boldPaint)
            canvas.drawText("No. Rekening: ${userProfile.accountNumber ?: "-"}", margin + 10f, currentY + 40f, boldPaint)
            canvas.drawText("Atas Nama: ${userProfile.accountName ?: userProfile.name}", margin + 10f, currentY + 50f, bodyPaint)

            currentY += 60f
        } else {
            canvas.drawText("Silakan hubungi penyedia jasa untuk rincian nomor rekening transfer.", margin, currentY + 12f, smallMutedPaint)
            currentY += 20f
        }

        return currentY
    }

    private fun drawNotesSection(canvas: Canvas, notes: String, y: Float): Float {
        var currentY = y
        canvas.drawText("CATATAN & SYARAT KETENTUAN:", margin, currentY, sectionHeaderPaint)
        currentY += 14f

        val lines = notes.chunked(70)
        lines.forEach { line ->
            canvas.drawText(line, margin, currentY, bodyPaint)
            currentY += 12f
        }

        return currentY
    }

    private fun drawSignatureSection(canvas: Canvas, userProfile: UserProfile?, y: Float) {
        val sigY = maxOf(y + 10f, pageHeight - margin - 90f)
        val rightX = pageWidth - margin - 20f

        val mcName = userProfile?.name ?: "MC Professional"
        val label = "Hormat Kami,"
        canvas.drawText(label, rightX - boldPaint.measureText(label) - 20f, sigY, bodyPaint)

        canvas.drawText(mcName, rightX - boldPaint.measureText(mcName) - 20f, sigY + 45f, boldPaint)
        canvas.drawText("Professional Master of Ceremony", rightX - smallMutedPaint.measureText("Professional Master of Ceremony") - 20f, sigY + 57f, smallMutedPaint)
    }

    private fun drawFooter(canvas: Canvas, y: Float) {
        canvas.drawLine(margin, y - 8f, pageWidth - margin, y - 8f, linePaint)
        canvas.drawText("Terima kasih atas kerja samanya. Dokumen ini sah dan diterbitkan secara digital melalui aplikasi mcjob.id.", margin, y + 4f, smallMutedPaint)
    }

    private fun savePdfToFile(pdfDocument: PdfDocument, invoiceNumber: String, bookingId: String): File {
        val invoicesDir = File(context.filesDir, "invoices")
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs()
        }

        val sanitizedNumber = invoiceNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "invoice_${sanitizedNumber}_$bookingId.pdf"
        val file = File(invoicesDir, fileName)

        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        return file
    }

    fun shareInvoice(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Bagikan Invoice PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }

    fun viewInvoice(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
