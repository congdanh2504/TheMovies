package com.practice.network

import com.practice.network.model.NetworkCastResponse
import com.practice.network.model.NetworkMovie
import com.practice.network.model.NetworkMovieDetail
import com.practice.network.model.NetworkReview
import com.practice.network.model.PageResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TheMoviesApi {

    @GET("movie/now_playing")
    suspend fun getNowPlaying(): PageResponse<NetworkMovie>

    @GET("movie/popular")
    suspend fun getPopular(): PageResponse<NetworkMovie>

    @GET("movie/top_rated")
    suspend fun getTopRated(): PageResponse<NetworkMovie>

    @GET("movie/upcoming")
    suspend fun getUpcoming(): PageResponse<NetworkMovie>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int
    ): NetworkMovieDetail

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): PageResponse<NetworkMovie>

    @GET("movie/{movie_id}/reviews")
    suspend fun getReviews(
        @Path("movie_id") movieId: Int
    ): PageResponse<NetworkReview>

    @GET("movie/{movie_id}/credits")
    suspend fun getCasts(
        @Path("movie_id") movieId: Int
    ): NetworkCastResponse
}
