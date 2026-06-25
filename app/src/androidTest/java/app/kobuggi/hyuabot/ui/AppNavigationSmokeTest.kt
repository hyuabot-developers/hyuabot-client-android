package app.kobuggi.hyuabot.ui

import android.Manifest
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.userDataStore
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationSmokeTest {
    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun prepareAppState() {
        context.getSharedPreferences("pref", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("languageSuggestionShown", true)
            .apply()

        runBlocking {
            context.userDataStore.edit { preferences ->
                preferences[booleanPreferencesKey("coachmark_initialized")] = true
                preferences[stringSetPreferencesKey("coachmarks_seen")] = setOf(
                    "shuttle",
                    "shuttle_realtime_updates",
                    "shuttle_timetable",
                    "shuttle_bus_alternative",
                    "bus",
                    "subway",
                    "cafeteria",
                    "menu",
                    "reading_room",
                    "map",
                    "setting",
                )
            }
        }
    }

    @Test
    fun topLevelTabsRender() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()))

            selectBottomTab(R.id.shuttleRealtimeFragment)
            onView(withId(R.id.viewPager)).check(matches(isDisplayed()))

            selectBottomTab(R.id.busRealtimeFragment)
            onView(withId(R.id.viewPager)).check(matches(isDisplayed()))

            selectBottomTab(R.id.subwayRealtimeFragment)
            onView(withId(R.id.viewPager)).check(matches(isDisplayed()))

            selectBottomTab(R.id.cafeteriaFragment)
            onView(withId(R.id.date_picker_layout)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            onView(withId(R.id.menu_recycler_view)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun menuDestinationsRender() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openMenuDestination(R.string.menu_book)
            onView(withId(R.id.reading_room_swipe_refresh_layout)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            openMenuDestination(R.string.menu_contact)
            onView(withId(R.id.contact_list_view)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            openMenuDestination(R.string.menu_calendar)
            onView(withId(R.id.calendar_view)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            openMenuDestination(R.string.menu_settings)
            onView(withId(R.id.setting_campus)).check(matches(isDisplayed()))
            onView(withId(R.id.setting_language)).check(matches(isDisplayed()))
            onView(withId(R.id.setting_theme)).check(matches(isDisplayed()))
        }
    }

    private fun selectBottomTab(itemId: Int) {
        onView(withId(itemId)).perform(click())
    }

    private fun openMenuDestination(titleRes: Int) {
        selectBottomTab(R.id.menuFragment)
        onView(withId(R.id.menu_recycler_view)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(titleRes)),
                click(),
            ),
        )
    }
}
