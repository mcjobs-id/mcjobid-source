package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients WHERE isArchived = 0 ORDER BY updatedAt DESC, name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE isArchived = 0 AND ownerId = :ownerId ORDER BY updatedAt DESC, name ASC")
    fun getAllClientsByOwner(ownerId: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients ORDER BY updatedAt DESC, name ASC")
    fun getAllClientsIncludingArchived(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE ownerId = :ownerId ORDER BY updatedAt DESC, name ASC")
    fun getAllClientsIncludingArchivedByOwner(ownerId: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE isArchived = 0 AND (name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE isArchived = 0 AND ownerId = :ownerId AND (name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')")
    fun searchClientsByOwner(query: String, ownerId: String): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("UPDATE clients SET isArchived = 1 WHERE id = :id")
    suspend fun archiveClient(id: String)

    @Query("UPDATE clients SET isArchived = 0 WHERE id = :id")
    suspend fun unarchiveClient(id: String)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE ownerId = :ownerId AND id NOT IN (:ids)")
    suspend fun deleteClientsNotInIds(ownerId: String, ids: List<String>)

    @Query("DELETE FROM clients WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)
}
