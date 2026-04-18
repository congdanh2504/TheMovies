package com.practice.data.repository

import com.practice.database.dao.WatchlistDao
import com.practice.database.entity.WatchlistEntity
import com.practice.domain.model.WatchlistMovie
import com.practice.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class LocalRepositoryImpl @Inject constructor(
    private val dao: WatchlistDao
) : LocalRepository {

    override suspend fun saveToWatchlist(movie: WatchlistMovie) = dao.insert(movie.toEntity())

    override suspend fun removeFromWatchlist(movieId: Int) = dao.deleteById(movieId)

    override fun getWatchlist(): Flow<List<WatchlistMovie>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun isInWatchlist(movieId: Int): Flow<Boolean> = dao.existsById(movieId)

    override suspend fun saveRating(movieId: Int, rating: Float) = dao.updateRating(movieId, rating)

    override fun getRating(movieId: Int): Flow<Float?> = dao.getRating(movieId).onStart { emit(null) }
}

private fun WatchlistEntity.toDomain() = WatchlistMovie(
    id = movieId,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    runtime = runtime,
    genre = genre,
    userRating = userRating
)

private fun WatchlistMovie.toEntity() = WatchlistEntity(
    movieId = id,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    runtime = runtime,
    genre = genre,
    userRating = userRating
)
