package com.practice.domain.usecase

import com.practice.domain.repository.LocalRepository
import javax.inject.Inject

class RemoveFromWatchlistUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    suspend operator fun invoke(movieId: Int) = repository.removeFromWatchlist(movieId)
}
