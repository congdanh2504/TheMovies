package com.practice.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.datastore.UserPreferencesRepository
import com.practice.domain.model.Movie
import com.practice.domain.usecase.GetNowPlayingMoviesUseCase
import com.practice.domain.usecase.GetPopularMoviesUseCase
import com.practice.domain.usecase.GetTopRatedMoviesUseCase
import com.practice.domain.usecase.GetUpcomingMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val upcomingMovies: List<Movie> = emptyList(),
        val nowPlayingMovies: List<Movie> = emptyList(),
        val topRatedMovies: List<Movie> = emptyList(),
        val popularMovies: List<Movie> = emptyList(),
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val selectedTabIndex = userPreferencesRepository.preferences
        .map { it.selectedHomeTab }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        fetchAllMovies()
    }

    fun onTabSelected(index: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setSelectedHomeTab(index)
        }
    }

    private fun fetchAllMovies() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            supervisorScope {
                val upcoming = async { getUpcomingMoviesUseCase() }
                val nowPlaying = async { getNowPlayingMoviesUseCase() }
                val topRated = async { getTopRatedMoviesUseCase() }
                val popular = async { getPopularMoviesUseCase() }

                val upcomingResult = upcoming.await()
                val nowPlayingResult = nowPlaying.await()
                val topRatedResult = topRated.await()
                val popularResult = popular.await()

                val allFailed = listOf(upcomingResult, nowPlayingResult, topRatedResult, popularResult)
                    .all { it.isFailure }

                _uiState.value = if (allFailed) {
                    HomeUiState.Error(upcomingResult.exceptionOrNull()?.message ?: "Unknown error")
                } else {
                    HomeUiState.Success(
                        upcomingMovies = upcomingResult.getOrElse { emptyList() },
                        nowPlayingMovies = nowPlayingResult.getOrElse { emptyList() },
                        topRatedMovies = topRatedResult.getOrElse { emptyList() },
                        popularMovies = popularResult.getOrElse { emptyList() },
                    )
                }
            }
        }
    }
}
