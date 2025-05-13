package com.practice.domain.usecase

import com.practice.domain.model.MovieDetail
import com.practice.domain.repository.MovieRepository
import javax.inject.Inject

class GetMovieDetailsUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): MovieDetail {
        return repository.getMovieDetails(movieId)
    }
}
