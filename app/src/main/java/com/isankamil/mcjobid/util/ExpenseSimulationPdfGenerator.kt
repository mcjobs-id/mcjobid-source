package com.isankamil.mcjobid.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.isankamil.mcjobid.domain.model.UserProfile
import com.isankamil.mcjobid.ui.screen.simulator.SimulationExpenseItem
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpenseSimulationPdfGenerator(private val context: Context) {

    private val pageWidth = 595 // A4 width in points (72 dpi)
    private val pageHeight = 842 // A4 height in points (72 dpi)
    private val margin = 40f
    private val contentWidth = pageWidth - (2 * margin)

    // Paints
    private val brandPaint = Paint().apply {
        color = Color.parseColor("#4F46E5") // Primary Indigo
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val titlePaint = Paint().apply {
        color = Color.parseColor("#0F172A") // Slate 900
        textSize = 15f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val subtitlePaint = Paint().apply {
        color = Color.parseColor("#64748B") // Slate 500
        textSize = 9.5f
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
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val bodyPaint = Paint().apply {
        color = Color.parseColor("#334155") // Slate 700
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }

    private val tableHeaderPaint = Paint().apply {
        color = Color.WHITE
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.parseColor("#E2E8F0") // Slate 200
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    fun generatePdf(
        jobTitle: String,
        grossFee: Long,
        expenseItems: List<SimulationExpenseItem>,
        totalExpenses: Long,
        netProfit: Long,
        marginPercentage: Double,
        recommendedMinFee: Long,
        userProfile: UserProfile?,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var yPos = margin

            // 1. Header Banner Background (Subtle Indigo)
            val headerBg = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, headerBg)

            // Header Brand
            canvas.drawText("MCJOB.ID", margin, yPos + 18f, brandPaint)
            val subBrandPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
                isAntiAlias = true
            }
            canvas.drawText("Professional MC Management & Event Intelligence", margin, yPos + 32f, subBrandPaint)

            // Right side: Document Title
            val docTitlePaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("SIMULASI BIAYA & PROFIT", pageWidth - margin, yPos + 18f, docTitlePaint)

            val dateStr = "Dibuat: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}"
            val docDatePaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText(dateStr, pageWidth - margin, yPos + 32f, docDatePaint)

            yPos = 130f

            // 2. Info Agenda
            canvas.drawText("INFORMASI AGENDA SIMULASI", margin, yPos, sectionHeaderPaint)
            yPos += 14f

            val agendaTitle = if (jobTitle.isBlank()) "Simulasi Agenda MC" else jobTitle
            canvas.drawText("Nama Agenda: $agendaTitle", margin, yPos, boldPaint)
            val mcName = userProfile?.name ?: "MC Professional"
            val mcSpec = "${userProfile?.specialization ?: "Wedding & Corporate"} · ${userProfile?.city ?: "Indonesia"}"
            canvas.drawText("Penyusun: $mcName ($mcSpec)", margin, yPos + 14f, bodyPaint)

            yPos += 36f

            // 3. Proyeksi Finansial Card (3 Columns)
            val cardPaint = Paint().apply {
                color = Color.parseColor("#4F46E5")
                style = Paint.Style.FILL
            }
            val cardRect = RectF(margin, yPos, pageWidth - margin, yPos + 75f)
            canvas.drawRoundRect(cardRect, 10f, 10f, cardPaint)

            val whiteHeader = Paint().apply {
                color = Color.WHITE.copyAlpha(0.75f)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val whiteValue = Paint().apply {
                color = Color.WHITE
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            // Col 1: Gross Fee
            canvas.drawText("HONORARIUM GROSS", margin + 14f, yPos + 26f, whiteHeader)
            canvas.drawText(Formatter.formatCurrency(grossFee), margin + 14f, yPos + 48f, whiteValue)

            // Col 2: Total Biaya
            val col2X = margin + (contentWidth / 3f) + 8f
            canvas.drawText("TOTAL EST. PENGELUARAN", col2X, yPos + 26f, whiteHeader)
            val expValuePaint = Paint(whiteValue).apply { color = Color.parseColor("#FCA5A5") }
            canvas.drawText(Formatter.formatCurrency(totalExpenses), col2X, yPos + 48f, expValuePaint)

            // Col 3: Net Profit
            val col3X = margin + (contentWidth * 2f / 3f) + 8f
            canvas.drawText("ESTIMASI LABA BERSIH", col3X, yPos + 26f, whiteHeader)
            canvas.drawText(Formatter.formatCurrency(netProfit), col3X, yPos + 48f, whiteValue)
            val marginLabelPaint = Paint().apply {
                color = Color.parseColor("#34D399")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("${String.format("%.1f", marginPercentage)}% Margin Profit", col3X, yPos + 63f, marginLabelPaint)

            yPos += 95f

            // 4. Tabel Rincian Pengeluaran
            canvas.drawText("RINCIAN ESTIMASI PENGELUARAN (${expenseItems.size} ITEM)", margin, yPos, sectionHeaderPaint)
            yPos += 12f

            // Table Header Bar
            val tableHeaderBg = Paint().apply {
                color = Color.parseColor("#1E293B")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(margin, yPos, pageWidth - margin, yPos + 24f), 6f, 6f, tableHeaderBg)

            canvas.drawText("No", margin + 10f, yPos + 16f, tableHeaderPaint)
            canvas.drawText("Item Pengeluaran", margin + 40f, yPos + 16f, tableHeaderPaint)
            canvas.drawText("Kategori", margin + 280f, yPos + 16f, tableHeaderPaint)

            val rightAlignHeader = Paint(tableHeaderPaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("Estimasi Biaya (Rp)", pageWidth - margin - 12f, yPos + 16f, rightAlignHeader)

            yPos += 28f

            val rowBgAlt = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }

            if (expenseItems.isEmpty()) {
                canvas.drawText("Belum ada rincian pengeluaran disimulasikan.", margin + 12f, yPos + 18f, subtitlePaint)
                yPos += 30f
            } else {
                val rightAlignBody = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
                val rightAlignBold = Paint(boldPaint).apply { textAlign = Paint.Align.RIGHT; color = Color.parseColor("#DC2626") }

                expenseItems.forEachIndexed { index, item ->
                    val rowHeight = 22f
                    if (index % 2 == 1) {
                        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, rowBgAlt)
                    }

                    canvas.drawText("${index + 1}", margin + 10f, yPos + 15f, bodyPaint)
                    canvas.drawText(item.name, margin + 40f, yPos + 15f, boldPaint)
                    canvas.drawText(item.category, margin + 280f, yPos + 15f, bodyPaint)
                    canvas.drawText(Formatter.formatCurrency(item.amount), pageWidth - margin - 12f, yPos + 15f, rightAlignBold)

                    yPos += rowHeight
                }
            }

            // Divider Line below table
            canvas.drawLine(margin, yPos + 4f, pageWidth - margin, yPos + 4f, linePaint)
            yPos += 18f

            // Total Biaya Summary Row
            val rightAlignSummary = Paint(boldPaint).apply { textAlign = Paint.Align.RIGHT; textSize = 11f }
            canvas.drawText("TOTAL ESTIMASI BIAYA:", pageWidth - margin - 140f, yPos, rightAlignSummary)
            val totalExpenseValuePaint = Paint().apply {
                color = Color.parseColor("#DC2626")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText(Formatter.formatCurrency(totalExpenses), pageWidth - margin - 12f, yPos, totalExpenseValuePaint)

            yPos += 35f

            // 5. Analisis Kelayakan & Rekomendasi
            val recBg = Paint().apply {
                color = Color.parseColor("#FEF3C7") // Amber 100
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(margin, yPos, pageWidth - margin, yPos + 55f), 8f, 8f, recBg)

            val recTitlePaint = Paint().apply {
                color = Color.parseColor("#92400E") // Amber 800
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("💡 REKOMENDASI TARIF MINIMAL PENAWARAN (MIN. MARGIN 70%)", margin + 12f, yPos + 18f, recTitlePaint)

            val recDescPaint = Paint().apply {
                color = Color.parseColor("#78350F") // Amber 900
                textSize = 9f
                isAntiAlias = true
            }
            val minFeeText = "Agar batas keuntungan tetap terjaga, rekomendasi tarif minimal adalah: ${Formatter.formatCurrency(recommendedMinFee)}"
            canvas.drawText(minFeeText, margin + 12f, yPos + 34f, recDescPaint)

            // 6. Footer & Timestamp
            val footerY = pageHeight - margin
            canvas.drawLine(margin, footerY - 14f, pageWidth - margin, footerY - 14f, linePaint)

            val footerText = "Dokumen ini dibuat otomatis via MCJOB.ID untuk perencanaan internal MC."
            canvas.drawText(footerText, margin, footerY, subtitlePaint)

            val pageNumberPaint = Paint(subtitlePaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("Halaman 1 / 1", pageWidth - margin, footerY, pageNumberPaint)

            pdfDocument.finishPage(page)

            // Save File
            val dir = File(context.cacheDir, "simulations").apply { mkdirs() }
            val cleanTitle = jobTitle.replace("[^a-zA-Z0-9]".toRegex(), "_").take(20)
            val file = File(dir, "Simulasi_Biaya_${cleanTitle}_${System.currentTimeMillis()}.pdf")

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            onSuccess(file)
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun openPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Tidak ada aplikasi pembaca PDF terpasang", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Bagikan Simulasi Biaya PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Int.copyAlpha(alpha: Float): Int {
        val a = (255 * alpha).toInt()
        return (this and 0x00FFFFFF) or (a shl 24)
    }
}
