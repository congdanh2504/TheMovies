package com.practice.domain.usecase

import com.practice.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRatingUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    operator fun invoke(movieId: Int): Flow<Float?> = repository.getRating(movieId)
}
