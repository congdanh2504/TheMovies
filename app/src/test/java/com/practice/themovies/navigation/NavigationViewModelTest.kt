package com.practice.themovies.navigation

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationViewModelTest {

    private fun viewModel() = NavigationViewModel(SavedStateHandle())

    @Test
    fun `initial back stack contains HomeDestination`() {
        val vm = viewModel()
        assertEquals(listOf(HomeDestination), vm.backStack.toList())
    }

    @Test
    fun `navigate pushes destination onto back stack`() {
        val vm = viewModel()
        vm.navigate(DetailDestination(42))
        assertEquals(listOf(HomeDestination, DetailDestination(42)), vm.backStack.toList())
    }

    @Test
    fun `popBack removes last destination`() {
        val vm = viewModel()
        vm.navigate(SearchDestination)
        vm.popBack()
        assertEquals(listOf(HomeDestination), vm.backStack.toList())
    }

    @Test
    fun `popBack does not remove last item when stack has one entry`() {
        val vm = viewModel()
        vm.popBack()
        assertEquals(listOf(HomeDestination), vm.backStack.toList())
    }

    @Test
    fun `navigateToTab clears stack and pushes tab destination`() {
        val vm = viewModel()
        vm.navigate(DetailDestination(1))
        vm.navigateToTab(SearchDestination)
        assertEquals(listOf(SearchDestination), vm.backStack.toList())
    }

    @Test
    fun `navigateToTab from nested stack replaces entire stack`() {
        val vm = viewModel()
        vm.navigate(SearchDestination)
        vm.navigate(DetailDestination(5))
        vm.navigateToTab(WatchlistDestination)
        assertEquals(listOf(WatchlistDestination), vm.backStack.toList())
    }

    @Test
    fun `back stack is restored from SavedStateHandle`() {
        val handle = SavedStateHandle()
        val vm1 = NavigationViewModel(handle)
        vm1.navigate(SearchDestination)
        vm1.navigate(DetailDestination(99))

        // Simulate process death/restoration using the same handle
        val vm2 = NavigationViewModel(handle)
        assertEquals(listOf(HomeDestination, SearchDestination, DetailDestination(99)), vm2.backStack.toList())
    }
}
