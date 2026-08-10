package com.isankamil.mcjobid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.isankamil.mcjobid.data.local.dao.RateCardDao
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.RateCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateCardRepository @Inject constructor(
    private val rateCardDao: RateCardDao,
    private val firestoreSyncService: FirestoreSyncService,
    private val auth: FirebaseAuth
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rateCardDao.deleteDefaultRateCards()
            } catch (_: Exception) {}
        }
    }

    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getRateCards(): Flow<List<RateCard>> {
        val uid = getCurrentUserId()
        return rateCardDao.getRateCardsByOwner(uid).map { entities ->
            entities.map { RateCard.fromEntity(it) }
        }
    }

    suspend fun getRateCardById(id: String): RateCard? {
        return rateCardDao.getRateCardById(id)?.let { RateCard.fromEntity(it) }
    }

    suspend fun saveRateCard(rateCard: RateCard) {
        val uid = getCurrentUserId()
        val entity = rateCard.toEntity().copy(ownerId = uid, isDefault = false)
        rateCardDao.insertRateCard(entity)
        try {
            firestoreSyncService.saveRateCardToFirestore(entity)
        } catch (_: Exception) {}
    }

    suspend fun deleteRateCard(id: String) {
        rateCardDao.deleteRateCard(id)
        try {
            firestoreSyncService.deleteRateCardFromFirestore(id)
        } catch (_: Exception) {}
    }
}
