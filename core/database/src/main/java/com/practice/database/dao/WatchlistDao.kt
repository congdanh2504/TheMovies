package com.practice.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practice.database.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun deleteById(movieId: Int)

    @Query("SELECT * FROM watchlist ORDER BY title ASC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE movieId = :movieId)")
    fun existsById(movieId: Int): Flow<Boolean>

    @Query("UPDATE watchlist SET userRating = :rating WHERE movieId = :movieId")
    suspend fun updateRating(movieId: Int, rating: Float)
}
