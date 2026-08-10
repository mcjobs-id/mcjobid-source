package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?
    
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfileFlow(userId: String): Flow<UserProfileEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)
    
    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)
    
    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)
    
    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteUserProfileById(userId: String)
}

