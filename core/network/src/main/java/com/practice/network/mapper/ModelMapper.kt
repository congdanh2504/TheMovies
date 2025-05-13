package com.practice.network.mapper

import com.practice.domain.model.Cast
import com.practice.domain.model.Genre
import com.practice.domain.model.Movie
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.Review
import com.practice.domain.model.MoviesPage
import com.practice.network.model.NetworkCast
import com.practice.network.model.NetworkGenre
import com.practice.network.model.NetworkMovie
import com.practice.network.model.NetworkMovieDetail
import com.practice.network.model.NetworkReview
import com.practice.network.model.PageResponse

private const val baseImageUrl = "https://image.tmdb.org/t/p/w500"

fun NetworkMovie.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    posterPath = baseImageUrl + posterPath,
    backdropPath = baseImageUrl + backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount
)

fun NetworkMovieDetail.toDomain(): MovieDetail = MovieDetail(
    id = id,
    backdropPath = baseImageUrl + backdropPath,
    genres = genres.map { it.toDomain() },
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    overview = overview,
    video = video,
    posterPath = posterPath,
    releaseDate = releaseDate,
    runtime = runtime,
    title = title,
    status = status,
    voteAverage = voteAverage,
    voteCount = voteCount
)

fun NetworkGenre.toDomain(): Genre = Genre(
    id = id,
    name = name
)

fun NetworkCast.toDomain(): Cast = Cast(
    castId = castId,
    character = character,
    name = name,
    profilePath = baseImageUrl + profilePath
)

fun NetworkReview.toDomain(): Review = Review(
    author = author,
    content = content,
    createdAt = createdAt
)

fun PageResponse<NetworkMovie>.toDomain(): MoviesPage = MoviesPage(
    page = page,
    totalPages = totalPages,
    totalResults = totalResults,
    results = results.map { it.toDomain() }
)
