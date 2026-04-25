package com.practice.domain.usecase

import androidx.paging.PagingData
import com.practice.domain.model.Movie
import com.practice.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMoviesPagedUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> =
        movieRepository.searchMoviesPaged(query)
}
