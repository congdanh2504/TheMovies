package com.practice.domain.usecase

import com.practice.domain.model.Movie
import com.practice.domain.repository.MovieRepository
import javax.inject.Inject

class GetUpcomingMoviesUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(): List<Movie> {
        return repository.getUpcomingMovies()
    }
}
