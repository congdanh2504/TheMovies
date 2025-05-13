package com.practice.data.repository

import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.MoviesPage
import com.practice.domain.repository.MovieRepository
import com.practice.network.TheMoviesApi
import com.practice.network.mapper.toDomain
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val theMoviesApi: TheMoviesApi
) : MovieRepository {

    override suspend fun getNowPlayingMovies(): List<Movie> {
        return theMoviesApi.getNowPlaying()
            .results
            .map { it.toDomain() }
    }

    override suspend fun getUpcomingMovies(): List<Movie> {
        return theMoviesApi.getUpcoming()
            .results
            .map { it.toDomain() }
    }

    override suspend fun getTopRatedMovies(): List<Movie> {
        return theMoviesApi.getTopRated()
            .results
            .map { it.toDomain() }
    }

    override suspend fun getPopularMovies(): List<Movie> {
        return theMoviesApi.getPopular()
            .results
            .map { it.toDomain() }
    }

    override suspend fun getMovieDetails(movieId: Int): MovieDetail {
        return theMoviesApi.getMovieDetails(movieId)
            .toDomain()
    }

    override suspend fun searchMovies(query: String, page: Int): MoviesPage {
        return theMoviesApi.searchMovies(query, page).toDomain()
    }
}
