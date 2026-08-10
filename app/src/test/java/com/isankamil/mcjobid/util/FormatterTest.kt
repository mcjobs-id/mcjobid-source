package com.isankamil.mcjobid.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatterTest {

    @Test
    fun formatWhatsAppNumber_preservesInternationalPrefix() {
        assertEquals("6281234567890", Formatter.formatWhatsAppNumber("6281234567890"))
    }

    @Test
    fun formatWhatsAppNumber_convertsLeadingZero() {
        assertEquals("6281234567890", Formatter.formatWhatsAppNumber("081234567890"))
    }

    @Test
    fun formatWhatsAppNumber_prependsCountryCodeForLocalNumber() {
        assertEquals("6281234567890", Formatter.formatWhatsAppNumber("81234567890"))
    }

    @Test
    fun formatWhatsAppNumber_stripsNonDigitSeparators() {
        assertEquals("6281234567890", Formatter.formatWhatsAppNumber("+62 812-3456-7890"))
    }

    @Test
    fun formatWhatsAppNumber_returnsEmptyForBlank() {
        assertEquals("", Formatter.formatWhatsAppNumber(""))
        assertEquals("", Formatter.formatWhatsAppNumber(null))
    }

    @Test
    fun formatWhatsAppNumber_preservesOtherDigits() {
        assertEquals("123456", Formatter.formatWhatsAppNumber("123456"))
    }
}
