package com.practice.search

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practice.domain.model.Movie
import com.practice.domain.usecase.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent()
    object LoadMore : SearchIntent()
}

data class SearchState(
    val query: String = "",
    val movies: List<Movie> = emptyList(),
    val page: Int = 1,
    val totalPages: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _intents = Channel<SearchIntent>(Channel.UNLIMITED)

    init {
        handleIntents()
    }

    fun processIntent(intent: SearchIntent) {
        viewModelScope.launch {
            _intents.send(intent)
        }
    }

    private fun handleIntents() {
        viewModelScope.launch {
            _intents.consumeAsFlow()
                .collectLatest { intent ->
                    when (intent) {
                        is SearchIntent.QueryChanged -> {
                            _state.update { it.copy(query = intent.query) }
                            observeQueryChanges()
                        }
                        is SearchIntent.LoadMore -> loadMore()
                    }
                }
        }
    }

    private var queryJob: Job? = null
    private fun observeQueryChanges() {
        queryJob?.cancel()
        queryJob = viewModelScope.launch {
            snapshotFlow { _state.value.query }
                .debounce(1000) // debounce 1s
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { q ->
                    search(query = q, page = 1, reset = true)
                }
        }
    }

    private fun search(query: String, page: Int, reset: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val result = searchMoviesUseCase(query, page)
                _state.update {
                    it.copy(
                        movies = if (reset) result.results else it.movies + result.results,
                        page = page,
                        totalPages = result.totalPages,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadMore() {
        val s = _state.value
        if (!s.isLoading && s.page < s.totalPages) {
            search(s.query, s.page + 1, reset = false)
        }
    }
}

