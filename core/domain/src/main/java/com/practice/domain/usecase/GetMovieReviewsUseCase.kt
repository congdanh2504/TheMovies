package com.practice.domain.usecase

import com.practice.domain.model.Review
import com.practice.domain.repository.MovieRepository
import javax.inject.Inject

class GetMovieReviewsUseCase @Inject constructor(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): List<Review> = repository.getReviews(movieId)
}
