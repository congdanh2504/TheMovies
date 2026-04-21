package com.practice.themovies.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    private val _backStack = mutableListOf<Any>(HomeDestination)
    val backStack: MutableList<Any> get() = _backStack

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
