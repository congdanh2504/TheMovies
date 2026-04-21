package com.practice.themovies.navigation

import com.practice.themovies.R
import kotlinx.serialization.Serializable

@Serializable object HomeDestination
@Serializable object SearchDestination
@Serializable object WatchlistDestination
@Serializable data class DetailDestination(val movieId: Int)

sealed class BottomNavItem(val destination: Any, val icon: Int, val label: String) {
    object Home     : BottomNavItem(HomeDestination,     R.drawable.ic_home,   "Home")
    object Search   : BottomNavItem(SearchDestination,   R.drawable.ic_search, "Search")
    object Watchlist: BottomNavItem(WatchlistDestination, R.drawable.ic_save,  "Watch List")
}
