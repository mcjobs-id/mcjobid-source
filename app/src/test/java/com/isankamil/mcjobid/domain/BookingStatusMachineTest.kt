package com.isankamil.mcjobid

import com.isankamil.mcjobid.domain.model.Booking.BookingStatus
import com.isankamil.mcjobid.domain.usecase.job.BookingStatusMachine
import org.junit.Assert.*
import org.junit.Test

class BookingStatusMachineTest {

    @Test
    fun testValidStatusTransitions() {
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.DRAFT, BookingStatus.CONFIRMED))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.CONFIRMED, BookingStatus.TODAY))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.TODAY, BookingStatus.COMPLETED))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.CONFIRMED, BookingStatus.CANCELLED))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.CONFIRMED, BookingStatus.ACTIVE))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.UPCOMING, BookingStatus.ACTIVE))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.TODAY, BookingStatus.ACTIVE))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.ACTIVE, BookingStatus.COMPLETED))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.ACTIVE, BookingStatus.CANCELLED))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.ACTIVE, BookingStatus.CONFIRMED))
    }

    @Test
    fun testSameStatusTransitionAllowed() {
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.CONFIRMED, BookingStatus.CONFIRMED))
        assertTrue(BookingStatusMachine.isValidTransition(BookingStatus.COMPLETED, BookingStatus.COMPLETED))
    }

    @Test
    fun testInvalidStatusTransitions() {
        assertFalse(BookingStatusMachine.isValidTransition(BookingStatus.COMPLETED, BookingStatus.CANCELLED))
        assertFalse(BookingStatusMachine.isValidTransition(BookingStatus.DRAFT, BookingStatus.COMPLETED))
        assertFalse(BookingStatusMachine.isValidTransition(BookingStatus.DRAFT, BookingStatus.ACTIVE))
    }
}
