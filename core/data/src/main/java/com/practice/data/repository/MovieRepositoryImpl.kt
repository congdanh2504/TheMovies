package com.practice.data.repository

import com.practice.domain.model.Cast
import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.MoviesPage
import com.practice.domain.model.Review
import com.practice.domain.repository.MovieRepository
import com.practice.network.TheMoviesApi
import com.practice.network.mapper.toDomain
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val theMoviesApi: TheMoviesApi
) : MovieRepository {

    override suspend fun getNowPlayingMovies(): List<Movie> =
        theMoviesApi.getNowPlaying().results.map { it.toDomain() }

    override suspend fun getUpcomingMovies(): List<Movie> =
        theMoviesApi.getUpcoming().results.map { it.toDomain() }

    override suspend fun getTopRatedMovies(): List<Movie> =
        theMoviesApi.getTopRated().results.map { it.toDomain() }

    override suspend fun getPopularMovies(): List<Movie> =
        theMoviesApi.getPopular().results.map { it.toDomain() }

    override suspend fun getMovieDetails(movieId: Int): MovieDetail =
        theMoviesApi.getMovieDetails(movieId).toDomain()

    override suspend fun searchMovies(query: String, page: Int): MoviesPage =
        theMoviesApi.searchMovies(query, page).toDomain()

    override suspend fun getReviews(movieId: Int): List<Review> =
        theMoviesApi.getReviews(movieId).results.map { it.toDomain() }

    override suspend fun getCast(movieId: Int): List<Cast> =
        theMoviesApi.getCasts(movieId).cast.map { it.toDomain() }
}
