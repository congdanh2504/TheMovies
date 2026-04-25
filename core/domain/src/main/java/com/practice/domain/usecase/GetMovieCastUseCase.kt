package com.practice.domain.usecase

import com.practice.domain.repository.MovieRepository
import javax.inject.Inject

class GetMovieCastUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int) = repository.getCast(movieId)
}
