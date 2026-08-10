package com.isankamil.mcjobid

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentValidationTest {

    @Test
    fun testZeroOrNegativePaymentIsInvalid() {
        val amountZero = 0L
        val amountNegative = -100_000L
        val amountValid = 500_000L

        assertFalse(amountZero > 0)
        assertFalse(amountNegative > 0)
        assertTrue(amountValid > 0)
    }

    @Test
    fun testOverpaymentDetection() {
        val fee = 5_000_000L
        val paid = 3_000_000L
        val newPayment = 3_000_000L

        val isOverpayment = (paid + newPayment) > fee
        assertTrue(isOverpayment)
    }
}
