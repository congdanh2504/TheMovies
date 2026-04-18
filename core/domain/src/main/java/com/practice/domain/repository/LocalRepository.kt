package com.practice.domain.repository

import com.practice.domain.model.WatchlistMovie
import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    suspend fun saveToWatchlist(movie: WatchlistMovie)
    suspend fun removeFromWatchlist(movieId: Int)
    fun getWatchlist(): Flow<List<WatchlistMovie>>
    fun isInWatchlist(movieId: Int): Flow<Boolean>
    suspend fun saveRating(movieId: Int, rating: Float)
}