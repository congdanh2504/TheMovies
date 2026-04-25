package com.practice.themovies

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val APP_PACKAGE = "com.practice.themovies"
private const val LAUNCH_TIMEOUT = 5_000L
private const val UI_TIMEOUT = 10_000L

// Compose exposes testTag values as plain viewIdResourceName (no package prefix)
private val NAV_HOME = By.res("nav_home")
private val NAV_SEARCH = By.res("nav_search")
private val NAV_WATCHLIST = By.res("nav_watch_list")
private val MOVIE_CARD = By.res("movie_card")

@RunWith(AndroidJUnit4::class)
class AppUiTest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()

        val context = InstrumentationRegistry.getInstrumentation().context
        val intent = context.packageManager
            .getLaunchIntentForPackage(APP_PACKAGE)
            ?.apply { addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK) }
        context.startActivity(intent)

        device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun bottomNav_isVisible_onLaunch() {
        assertNotNull("Home nav missing",
            device.wait(Until.findObject(NAV_HOME), UI_TIMEOUT))
        assertNotNull("Search nav missing",
            device.findObject(NAV_SEARCH))
        assertNotNull("Watchlist nav missing",
            device.findObject(NAV_WATCHLIST))
    }

    @Test
    fun navigate_toSearch_viaBottomNav() {
        val searchTab = device.wait(Until.findObject(NAV_SEARCH), UI_TIMEOUT)
        assertNotNull("Search nav not found", searchTab)
        searchTab.click()

        val searchField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), UI_TIMEOUT)
        assertNotNull("Search text field not visible after tapping Search nav", searchField)
    }

    @Test
    fun search_typeQuery_showsResults() {
        device.wait(Until.findObject(NAV_SEARCH), UI_TIMEOUT)?.click()

        val searchField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), UI_TIMEOUT)
        assertNotNull("Search text field not found", searchField)
        searchField.click()
        searchField.text = "Batman"

        // Wait for debounce + network: scrollable list or no-result indicator
        val hasScrollable = device.wait(Until.hasObject(By.scrollable(true)), UI_TIMEOUT)
        val hasNoResult = device.findObject(By.desc("No Result")) != null
        assertTrue("Neither results list nor no-result state appeared", hasScrollable || hasNoResult)
    }

    @Test
    fun navigate_toWatchlist_viaBottomNav() {
        val watchlistTab = device.wait(Until.findObject(NAV_WATCHLIST), UI_TIMEOUT)
        assertNotNull("Watchlist nav not found", watchlistTab)
        watchlistTab.click()

        assertNotNull("Bottom nav disappeared on Watchlist screen",
            device.wait(Until.findObject(NAV_HOME), UI_TIMEOUT))
    }

    @Test
    fun navigate_backToHome_fromDetail() {
        // Detail is pushed on top of Home, so back press should restore the Home nav bar
        val movieCard = device.wait(Until.findObject(MOVIE_CARD), UI_TIMEOUT)
        assertNotNull("No movie card found", movieCard)
        movieCard.click()

        // Wait until detail screen hides the nav bar
        device.wait(Until.gone(NAV_HOME), UI_TIMEOUT)

        device.pressBack()

        assertNotNull("Home nav not restored after back press from detail",
            device.wait(Until.findObject(NAV_HOME), UI_TIMEOUT))
    }

    @Test
    fun bottomNav_hidden_onDetailScreen() {
        val movieCard = device.wait(Until.findObject(MOVIE_CARD), UI_TIMEOUT)
        assertNotNull("No movie card found on home screen", movieCard)
        movieCard.click()

        val bottomNavGone = device.wait(Until.gone(NAV_SEARCH), UI_TIMEOUT)
        assertTrue("Bottom nav should be hidden on detail screen", bottomNavGone)
    }
}
