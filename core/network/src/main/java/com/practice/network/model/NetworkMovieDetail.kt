package com.practice.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkMovieDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "genres") val genres: List<Genre>,
    @Json(name = "original_title") val originalTitle: String,
    @Json(name = "original_language") val originalLanguage: String,
    @Json(name = "overview") val overview: String?,
    @Json(name = "video") val video: Boolean,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "release_date") val releaseDate: String,
    @Json(name = "runtime") val runtime: Long?,
    @Json(name = "title") val title: String,
    @Json(name = "status") val status: String,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "vote_count") val voteCount: Int
)

@JsonClass(generateAdapter = true)
data class Genre(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)
