package com.practice.domain.repository

import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.MoviesPage

interface MovieRepository {
    suspend fun getMovieDetails(movieId: Int): MovieDetail
    suspend fun searchMovies(query: String, page: Int): MoviesPage
    suspend fun getPopularMovies(): List<Movie>
    suspend fun getTopRatedMovies(): List<Movie>
    suspend fun getUpcomingMovies(): List<Movie>
    suspend fun getNowPlayingMovies(): List<Movie>
}
