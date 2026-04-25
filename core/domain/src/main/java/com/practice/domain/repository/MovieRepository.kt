package com.practice.domain.repository

import androidx.paging.PagingData
import com.practice.domain.model.Cast
import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.MoviesPage
import com.practice.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getMovieDetails(movieId: Int): Result<MovieDetail>
    suspend fun searchMovies(query: String, page: Int): Result<MoviesPage>
    fun searchMoviesPaged(query: String): Flow<PagingData<Movie>>
    suspend fun getPopularMovies(): Result<List<Movie>>
    suspend fun getTopRatedMovies(): Result<List<Movie>>
    suspend fun getUpcomingMovies(): Result<List<Movie>>
    suspend fun getNowPlayingMovies(): Result<List<Movie>>
    suspend fun getReviews(movieId: Int): Result<List<Review>>
    suspend fun getCast(movieId: Int): Result<List<Cast>>
}
