package com.practice.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.practice.data.paging.MoviePagingSource
import com.practice.domain.model.Cast
import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.MoviesPage
import com.practice.domain.model.Review
import com.practice.domain.repository.MovieRepository
import com.practice.network.TheMoviesApi
import com.practice.network.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val theMoviesApi: TheMoviesApi
) : MovieRepository {

    override suspend fun getNowPlayingMovies(): Result<List<Movie>> =
        runCatching { theMoviesApi.getNowPlaying().results.map { it.toDomain() } }

    override suspend fun getUpcomingMovies(): Result<List<Movie>> =
        runCatching { theMoviesApi.getUpcoming().results.map { it.toDomain() } }

    override suspend fun getTopRatedMovies(): Result<List<Movie>> =
        runCatching { theMoviesApi.getTopRated().results.map { it.toDomain() } }

    override suspend fun getPopularMovies(): Result<List<Movie>> =
        runCatching { theMoviesApi.getPopular().results.map { it.toDomain() } }

    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetail> =
        runCatching { theMoviesApi.getMovieDetails(movieId).toDomain() }

    override suspend fun searchMovies(query: String, page: Int): Result<MoviesPage> =
        runCatching { theMoviesApi.searchMovies(query, page).toDomain() }

    override fun searchMoviesPaged(query: String): Flow<PagingData<Movie>> =
        Pager(PagingConfig(pageSize = 20, enablePlaceholders = false)) {
            MoviePagingSource(theMoviesApi, query)
        }.flow

    override suspend fun getReviews(movieId: Int): Result<List<Review>> =
        runCatching { theMoviesApi.getReviews(movieId).results.map { it.toDomain() } }

    override suspend fun getCast(movieId: Int): Result<List<Cast>> =
        runCatching { theMoviesApi.getCasts(movieId).cast.map { it.toDomain() } }
}
