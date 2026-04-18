package com.practice.domain.usecase

import com.practice.domain.model.WatchlistMovie
import com.practice.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchlistUseCase @Inject constructor(
    private val repository: LocalRepository
) {
    operator fun invoke(): Flow<List<WatchlistMovie>> = repository.getWatchlist()
}
