package com.practice.detailmovie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.domain.model.Cast
import com.practice.domain.model.MovieDetail
import com.practice.domain.model.Review
import com.practice.domain.model.WatchlistMovie
import com.practice.domain.usecase.GetMovieCastUseCase
import com.practice.domain.usecase.GetMovieDetailsUseCase
import com.practice.domain.usecase.GetMovieReviewsUseCase
import com.practice.domain.usecase.GetRatingUseCase
import com.practice.domain.usecase.IsInWatchlistUseCase
import com.practice.domain.usecase.RemoveFromWatchlistUseCase
import com.practice.domain.usecase.SaveRatingUseCase
import com.practice.domain.usecase.SaveToWatchlistUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class DetailUiState(
    val movieDetail: MovieDetail? = null,
    val cast: List<Cast> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isInWatchlist: Boolean = false,
    val userRating: Float? = null,
    val showRatingDialog: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel(assistedFactory = DetailMovieViewModel.Factory::class)
class DetailMovieViewModel @AssistedInject constructor(
    @Assisted val movieId: Int,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val getMovieCastUseCase: GetMovieCastUseCase,
    private val getMovieReviewsUseCase: GetMovieReviewsUseCase,
    private val isInWatchlistUseCase: IsInWatchlistUseCase,
    private val saveToWatchlistUseCase: SaveToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val saveRatingUseCase: SaveRatingUseCase,
    private val getRatingUseCase: GetRatingUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): DetailMovieViewModel
    }

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadData()
        observeWatchlistStatus()
        observeRating()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            supervisorScope {
                val detailDeferred = async { getMovieDetailsUseCase(movieId) }
                val castDeferred = async { getMovieCastUseCase(movieId) }
                val reviewsDeferred = async { getMovieReviewsUseCase(movieId) }

                val detail = detailDeferred.await()
                val cast = castDeferred.await()
                val reviews = reviewsDeferred.await()

                _state.update {
                    it.copy(
                        movieDetail = detail.getOrNull(),
                        cast = cast.getOrElse { emptyList() },
                        reviews = reviews.getOrElse { emptyList() },
                        isLoading = false,
                        error = detail.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private fun observeWatchlistStatus() {
        viewModelScope.launch {
            isInWatchlistUseCase(movieId).collect { isIn ->
                _state.update { it.copy(isInWatchlist = isIn) }
            }
        }
    }

    private fun observeRating() {
        viewModelScope.launch {
            getRatingUseCase(movieId).collect { rating ->
                _state.update { it.copy(userRating = rating) }
            }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            val current = _state.value
            val detail = current.movieDetail ?: return@launch
            if (current.isInWatchlist) {
                removeFromWatchlistUseCase(movieId)
            } else {
                saveToWatchlistUseCase(detail.toWatchlistMovie())
            }
        }
    }

    fun showRatingDialog() = _state.update { it.copy(showRatingDialog = true) }

    fun dismissRatingDialog() = _state.update { it.copy(showRatingDialog = false) }

    fun submitRating(rating: Float) {
        viewModelScope.launch {
            val current = _state.value
            val detail = current.movieDetail ?: return@launch
            if (!current.isInWatchlist) {
                saveToWatchlistUseCase(detail.toWatchlistMovie())
            }
            saveRatingUseCase(movieId, rating)
            _state.update { it.copy(showRatingDialog = false) }
        }
    }

    private fun MovieDetail.toWatchlistMovie() = WatchlistMovie(
        id = id,
        title = title,
        posterPath = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        runtime = runtime?.toInt() ?: 0,
        genre = genres.firstOrNull()?.name ?: ""
    )
}
