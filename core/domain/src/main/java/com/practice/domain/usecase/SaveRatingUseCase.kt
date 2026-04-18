package com.practice.domain.usecase

import com.practice.domain.repository.LocalRepository
import javax.inject.Inject

class SaveRatingUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    suspend operator fun invoke(movieId: Int, rating: Float) = repository.saveRating(movieId, rating)
}
