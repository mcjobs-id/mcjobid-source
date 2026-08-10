package com.isankamil.mcjobid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.isankamil.mcjobid.data.local.dao.ClientDao
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.Client
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val auth: FirebaseAuth
) {
    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getAllClients(): Flow<List<Client>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            clientDao.getAllClientsByOwner(uid).map { entities ->
                entities.map { Client.fromEntity(it) }
            }
        } else {
            clientDao.getAllClients().map { entities ->
                entities.map { Client.fromEntity(it) }
            }
        }
    }

    fun getAllClientsIncludingArchived(): Flow<List<Client>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            clientDao.getAllClientsIncludingArchivedByOwner(uid).map { entities ->
                entities.map { Client.fromEntity(it) }
            }
        } else {
            clientDao.getAllClientsIncludingArchived().map { entities ->
                entities.map { Client.fromEntity(it) }
            }
        }
    }

    suspend fun getClientById(id: String): Client? {
        return clientDao.getClientById(id)?.let { Client.fromEntity(it) }
    }

    fun searchClients(query: String): Flow<List<Client>> {
        val uid = getCurrentUserId()
        return if (uid.isNotBlank()) {
            clientDao.searchClientsByOwner(query, uid).map { entities ->
                entities.map { Client.fromEntity(it) }
            }
        } else {
            clientDao.searchClients(query).map { entities ->
                entities.map { Client.fromEntity(it) }
            }
        }
    }

    suspend fun saveClient(client: Client) {
        val entity = client.toEntity().copy(ownerId = getCurrentUserId())
        clientDao.insertClient(entity)
        firestoreSyncService.saveClientToFirestore(entity)
    }

    suspend fun updateClient(client: Client) {
        val entity = client.toEntity().copy(ownerId = getCurrentUserId())
        clientDao.updateClient(entity)
        firestoreSyncService.saveClientToFirestore(entity)
    }

    suspend fun toggleFavoriteClient(id: String) {
        val client = getClientById(id)
        if (client != null) {
            val updated = client.copy(isFavorite = !client.isFavorite)
            val entity = updated.toEntity().copy(ownerId = getCurrentUserId())
            clientDao.updateClient(entity)
            firestoreSyncService.saveClientToFirestore(entity)
        }
    }

    suspend fun archiveClient(id: String) {
        clientDao.archiveClient(id)
        getClientById(id)?.let {
            val entity = it.toEntity().copy(ownerId = getCurrentUserId())
            firestoreSyncService.saveClientToFirestore(entity)
        }
    }

    suspend fun unarchiveClient(id: String) {
        clientDao.unarchiveClient(id)
        getClientById(id)?.let {
            val entity = it.toEntity().copy(ownerId = getCurrentUserId())
            firestoreSyncService.saveClientToFirestore(entity)
        }
    }

    suspend fun deleteClient(client: Client) {
        clientDao.deleteClient(client.toEntity())
        firestoreSyncService.deleteClientFromFirestore(client.id)
    }
}
