package com.isankamil.mcjobid.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object Formatter {
    
    private val indonesianLocale = Locale.forLanguageTag("id-ID")
    
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
        maximumFractionDigits = 0
    }
    
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", indonesianLocale)
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", indonesianLocale)
    private val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", indonesianLocale)
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", indonesianLocale)
    private val yearMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM", indonesianLocale)
    
    fun formatCurrency(amount: Long): String {
        return currencyFormat.format(amount)
    }
    
    fun formatDate(date: LocalDate): String {
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, indonesianLocale)
        return "$dayName, ${date.format(dateFormatter)}"
    }
    
    fun formatDateShort(date: LocalDate): String {
        return date.format(dateFormatter)
    }
    
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
    
    fun formatMonthYear(date: LocalDate): String {
        return date.format(monthYearFormatter)
    }
    
    fun formatMonthYear(month: String): String {
        val yearMonth = java.time.YearMonth.parse(month, yearMonthFormatter)
        return yearMonth.format(monthYearFormatter)
    }
    
    fun formatTime(time: String): String {
        return time // Assuming input is already in HH:mm format
    }
    
    fun formatYearMonth(date: LocalDate): String {
        return date.format(yearMonthFormatter)
    }
    
    fun getCurrentYearMonth(): String {
        return LocalDate.now().format(yearMonthFormatter)
    }
    
    fun getMonthName(month: Int): String {
        return java.time.Month.of(month).getDisplayName(TextStyle.FULL, indonesianLocale)
    }
    
    fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, dateFormatter)
    }
    
    fun parseYearMonth(yearMonthString: String): java.time.YearMonth {
        return java.time.YearMonth.parse(yearMonthString, yearMonthFormatter)
    }

    fun formatWhatsAppNumber(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.startsWith("62") -> digits
            digits.startsWith("0") -> "62" + digits.substring(1)
            digits.startsWith("8") -> "62$digits"
            else -> digits
        }
    }
}

