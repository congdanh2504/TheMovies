package com.practice.domain.model

data class MovieDetail(
    val id: Int,
    val backdropPath: String?,
    val genres: List<Genre>,
    val originalTitle: String,
    val originalLanguage: String,
    val overview: String?,
    val video: Boolean,
    val posterPath: String?,
    val releaseDate: String,
    val runtime: Long?,
    val title: String,
    val status: String,
    val voteAverage: Double,
    val voteCount: Int
)

data class Genre(
    val id: Int,
    val name: String
)
