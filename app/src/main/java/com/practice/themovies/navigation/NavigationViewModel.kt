package com.practice.themovies.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    val backStack = mutableStateListOf<Any>(HomeDestination)

    fun navigate(destination: Any) {
        backStack.add(destination)
    }

    fun navigateToTab(destination: Any) {
        backStack.clear()
        backStack.add(destination)
    }

    fun popBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
    }
}
