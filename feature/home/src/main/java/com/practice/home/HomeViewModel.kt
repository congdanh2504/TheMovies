package com.practice.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.domain.model.Movie
import com.practice.domain.usecase.GetNowPlayingMoviesUseCase
import com.practice.domain.usecase.GetPopularMoviesUseCase
import com.practice.domain.usecase.GetTopRatedMoviesUseCase
import com.practice.domain.usecase.GetUpcomingMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchAllMovies()
    }

    private fun fetchAllMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                supervisorScope {
                    val upcoming = async { getUpcomingMoviesUseCase() }
                    val nowPlaying = async { getNowPlayingMoviesUseCase() }
                    val topRated = async { getTopRatedMoviesUseCase() }
                    val popular = async { getPopularMoviesUseCase() }

                    _uiState.update {
                        it.copy(
                            upcomingMovies = upcoming.await(),
                            nowPlayingMovies = nowPlaying.await(),
                            topRatedMovies = topRated.await(),
                            popularMovies = popular.await(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }
}

data class HomeUiState(
    val upcomingMovies: List<Movie> = emptyList(),
    val nowPlayingMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val popularMovies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
