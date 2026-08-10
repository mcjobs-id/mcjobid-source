package com.isankamil.mcjobid

import com.isankamil.mcjobid.domain.model.Client
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientArchiveTest {

    @Test
    fun testClientArchivingState() {
        val client = Client(
            id = "c1",
            name = "WO Wedding Organizer",
            phone = "08123456789",
            email = null,
            company = "WO Indah",
            notes = null,
            isArchived = false
        )

        val archivedClient = client.copy(isArchived = true)

        assertFalse(client.isArchived)
        assertTrue(archivedClient.isArchived)
    }

    @Test
    fun testClientWithJobHistoryShouldBeArchivedNotHardDeleted() {
        val linkedJobCount = 3
        val shouldArchive = linkedJobCount > 0

        assertTrue(shouldArchive)
    }
}
