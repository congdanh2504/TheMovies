package com.practice.domain.usecase

import com.practice.domain.model.MoviesPage
import com.practice.domain.repository.MovieRepository
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(query: String, page: Int): MoviesPage {
        return repository.searchMovies(query, page)
    }
}
