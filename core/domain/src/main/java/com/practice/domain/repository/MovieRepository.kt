package com.practice.domain.repository

import com.practice.domain.model.Cast
import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.MoviesPage
import com.practice.domain.model.Review

interface MovieRepository {
    suspend fun getMovieDetails(movieId: Int): MovieDetail
    suspend fun searchMovies(query: String, page: Int): MoviesPage
    suspend fun getPopularMovies(): List<Movie>
    suspend fun getTopRatedMovies(): List<Movie>
    suspend fun getUpcomingMovies(): List<Movie>
    suspend fun getNowPlayingMovies(): List<Movie>
    suspend fun getReviews(movieId: Int): List<Review>
    suspend fun getCast(movieId: Int): List<Cast>
}
