package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.RateCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RateCardDao {
    @Query("SELECT * FROM rate_cards WHERE isDefault = 0 AND ownerId != '' AND ownerId != 'system' ORDER BY category ASC, price DESC")
    fun getAllRateCards(): Flow<List<RateCardEntity>>

    @Query("SELECT * FROM rate_cards WHERE ownerId = :ownerId AND isDefault = 0 AND ownerId != '' AND ownerId != 'system' ORDER BY category ASC, price DESC")
    fun getRateCardsByOwner(ownerId: String): Flow<List<RateCardEntity>>

    @Query("SELECT * FROM rate_cards WHERE id = :id")
    suspend fun getRateCardById(id: String): RateCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRateCard(rateCard: RateCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRateCards(rateCards: List<RateCardEntity>)

    @Query("DELETE FROM rate_cards WHERE id = :id")
    suspend fun deleteRateCard(id: String)

    @Query("DELETE FROM rate_cards WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)

    @Query("DELETE FROM rate_cards WHERE isDefault = 1 OR ownerId = '' OR ownerId = 'system' OR id LIKE 'rc_%' OR id LIKE 'sample%'")
    suspend fun deleteDefaultRateCards()
}
