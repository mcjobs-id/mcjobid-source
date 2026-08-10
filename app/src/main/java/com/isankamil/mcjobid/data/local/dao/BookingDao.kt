package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    
    @Query("SELECT * FROM bookings ORDER BY date DESC, createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE ownerId = :ownerId ORDER BY date DESC, createdAt DESC")
    fun getAllBookingsByOwner(ownerId: String): Flow<List<BookingEntity>>
    
    @Query("SELECT * FROM bookings WHERE LOWER(status) NOT IN ('completed', 'done', 'cancelled') ORDER BY date ASC")
    fun getActiveBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE ownerId = :ownerId AND LOWER(status) NOT IN ('completed', 'done', 'cancelled') ORDER BY date ASC")
    fun getActiveBookingsByOwner(ownerId: String): Flow<List<BookingEntity>>
    
    @Query("SELECT * FROM bookings WHERE LOWER(status) IN ('completed', 'done') OR date < :today ORDER BY date DESC")
    fun getHistoryBookings(today: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE ownerId = :ownerId AND (LOWER(status) IN ('completed', 'done') OR date < :today) ORDER BY date DESC")
    fun getHistoryBookingsByOwner(today: String, ownerId: String): Flow<List<BookingEntity>>
    
    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: String): BookingEntity?
    
    @Query("SELECT * FROM bookings WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getBookingsByDateRange(startDate: String, endDate: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE ownerId = :ownerId AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getBookingsByDateRangeByOwner(startDate: String, endDate: String, ownerId: String): Flow<List<BookingEntity>>
    
    @Query("SELECT * FROM bookings WHERE date = :date ORDER BY start ASC")
    fun getBookingsByDate(date: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE ownerId = :ownerId AND date = :date ORDER BY start ASC")
    fun getBookingsByDateByOwner(date: String, ownerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE date = :date AND id != :excludeId")
    suspend fun getBookingsOnDate(date: String, excludeId: String = ""): List<BookingEntity>

    @Query("SELECT * FROM bookings WHERE ownerId = :ownerId AND date = :date AND id != :excludeId")
    suspend fun getBookingsOnDateByOwner(date: String, ownerId: String, excludeId: String = ""): List<BookingEntity>

    @Query("SELECT * FROM bookings WHERE client = :clientName OR clientId = :clientId ORDER BY date DESC")
    fun getBookingsByClient(clientName: String, clientId: String = ""): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE ownerId = :ownerId AND (client = :clientName OR clientId = :clientId) ORDER BY date DESC")
    fun getBookingsByClientByOwner(clientName: String, ownerId: String, clientId: String = ""): Flow<List<BookingEntity>>
    
    @Query("SELECT * FROM bookings WHERE name LIKE '%' || :query || '%' OR client LIKE '%' || :query || '%' OR loc LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchBookings(query: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE ownerId = :ownerId AND (name LIKE '%' || :query || '%' OR client LIKE '%' || :query || '%' OR loc LIKE '%' || :query || '%') ORDER BY date DESC")
    fun searchBookingsByOwner(query: String, ownerId: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)
    
    @Update
    suspend fun updateBooking(booking: BookingEntity)
    
    @Delete
    suspend fun deleteBooking(booking: BookingEntity)
    
    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: String)

    @Query("DELETE FROM bookings WHERE ownerId = :ownerId AND id NOT IN (:ids)")
    suspend fun deleteBookingsNotInIds(ownerId: String, ids: List<String>)

    @Query("DELETE FROM bookings WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)
    
    @Query("SELECT SUM(fee) FROM bookings WHERE LOWER(status) != 'cancelled'")
    suspend fun getTotalHonor(): Long?
    
    @Query("SELECT SUM(dp) FROM bookings WHERE LOWER(status) != 'cancelled'")
    suspend fun getTotalPaid(): Long?
    
    @Query("SELECT SUM(fee - dp) FROM bookings WHERE fee > dp AND LOWER(status) != 'cancelled'")
    suspend fun getTotalOutstanding(): Long?

    @Query("SELECT SUM(fee) FROM bookings WHERE date = :date AND LOWER(status) != 'cancelled'")
    suspend fun getTotalHonorByDate(date: String): Long?

    @Query("SELECT SUM(fee) FROM bookings WHERE date >= :startDate AND date <= :endDate AND LOWER(status) != 'cancelled'")
    suspend fun getTotalHonorByRange(startDate: String, endDate: String): Long?
    
    @Query("SELECT SUM(fee) FROM bookings WHERE strftime('%Y-%m', date) = :yearMonth AND LOWER(status) != 'cancelled'")
    suspend fun getTotalHonorByMonth(yearMonth: String): Long?
    
    @Query("SELECT SUM(dp) FROM bookings WHERE strftime('%Y-%m', date) = :yearMonth AND LOWER(status) != 'cancelled'")
    suspend fun getTotalPaidByMonth(yearMonth: String): Long?
    
    @Query("SELECT SUM(fee - dp) FROM bookings WHERE fee > dp AND strftime('%Y-%m', date) = :yearMonth AND LOWER(status) != 'cancelled'")
    suspend fun getTotalOutstandingByMonth(yearMonth: String): Long?
}
