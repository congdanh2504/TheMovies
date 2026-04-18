package com.practice.domain.usecase

import com.practice.domain.model.WatchlistMovie
import com.practice.domain.repository.LocalRepository
import javax.inject.Inject

class SaveToWatchlistUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    suspend operator fun invoke(movie: WatchlistMovie) = repository.saveToWatchlist(movie)
}
