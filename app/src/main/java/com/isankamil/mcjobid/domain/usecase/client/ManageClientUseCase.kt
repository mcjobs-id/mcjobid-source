package com.isankamil.mcjobid.domain.usecase.client

import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ClientRepository
import com.isankamil.mcjobid.domain.model.Client
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ManageClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository,
    private val bookingRepository: BookingRepository
) {
    suspend fun archiveClient(clientId: String): Result<Unit> {
        return try {
            clientRepository.archiveClient(clientId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClientSafely(client: Client): Result<String> {
        val linkedJobs = bookingRepository.getBookingsByClient(client.name, client.id).first()
        return if (linkedJobs.isNotEmpty()) {
            clientRepository.archiveClient(client.id)
            Result.success("ARCHIVED")
        } else {
            clientRepository.deleteClient(client)
            Result.success("DELETED")
        }
    }
}
