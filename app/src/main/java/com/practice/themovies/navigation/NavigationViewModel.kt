package com.practice.themovies.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val backStack = mutableStateListOf<Any>().apply {
        val saved = savedStateHandle.get<ArrayList<String>>(KEY_BACK_STACK)
        addAll(saved?.map { it.toDestination() } ?: listOf(HomeDestination))
    }

    fun navigate(destination: Any) {
        backStack.add(destination)
        persist()
    }

    fun navigateToTab(destination: Any) {
        backStack.clear()
        backStack.add(destination)
        persist()
    }

    fun popBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            persist()
        }
    }

    private fun persist() {
        savedStateHandle[KEY_BACK_STACK] = ArrayList(backStack.map { it.toKey() })
    }

    private fun Any.toKey(): String = when (this) {
        HomeDestination -> "Home"
        SearchDestination -> "Search"
        WatchlistDestination -> "Watchlist"
        is DetailDestination -> "Detail:$movieId"
        else -> "Home"
    }

    private fun String.toDestination(): Any = when {
        this == "Home" -> HomeDestination
        this == "Search" -> SearchDestination
        this == "Watchlist" -> WatchlistDestination
        startsWith("Detail:") -> DetailDestination(removePrefix("Detail:").toInt())
        else -> HomeDestination
    }

    companion object {
        private const val KEY_BACK_STACK = "backStack"
    }
}
