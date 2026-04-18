package com.practice.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.domain.model.WatchlistMovie
import com.practice.domain.usecase.GetWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
    getWatchlistUseCase: GetWatchlistUseCase
) : ViewModel() {

    val watchlist: StateFlow<List<WatchlistMovie>> = getWatchlistUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
