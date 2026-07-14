package app.kobuggi.hyuabot.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.userDataStore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
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

            selectBottomTab(R.id.homeFragment)
            onView(withId(R.id.home_swipe_refresh_layout)).check(matches(isDisplayed()))

            selectBottomTab(R.id.busRealtimeFragment)
            onView(withId(R.id.viewPager)).check(matches(isDisplayed()))

            selectBottomTab(R.id.subwayRealtimeFragment)
            onView(withId(R.id.viewPager)).check(matches(isDisplayed()))

            selectBottomTab(R.id.cafeteriaFragment)
            onView(withId(R.id.date_picker_layout)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            onView(withId(R.id.campus_tools_grid)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun homeDestinationGroupKeepsStableChildren() {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra("homeDebugDeparture", "terminal")

        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val group = activity.findViewById<MaterialButtonToggleGroup>(R.id.destination_group)
                val visibleChildCount = (0 until group.childCount).count { index ->
                    group.getChildAt(index).visibility == View.VISIBLE
                }

                assertEquals(4, group.childCount)
                assertEquals(1, visibleChildCount)
            }
        }
    }

    @Test
    fun menuDestinationsRender() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openCampusTool(R.id.campus_map_card)
            onView(withId(R.id.search_bar)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            openCampusTool(R.id.campus_reading_room_card)
            onView(withId(R.id.reading_room_swipe_refresh_layout)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            openCampusTool(R.id.campus_contact_card)
            onView(withId(R.id.contact_list_view)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            openCampusTool(R.id.campus_calendar_card)
            onView(withId(R.id.calendar_timeline_view)).check(matches(isDisplayed()))

            selectBottomTab(R.id.menuFragment)
            openMenuDestination(R.string.menu_settings)
            onView(withId(R.id.setting_campus)).check(matches(isDisplayed()))
            onView(withId(R.id.setting_language)).check(matches(isDisplayed()))
            onView(withId(R.id.setting_theme)).check(matches(isDisplayed()))
        }
    }

    private fun selectBottomTab(itemId: Int) {
        onView(withId(R.id.bottom_navigation)).perform(selectNavigationItem(itemId))
    }

    private fun selectNavigationItem(itemId: Int): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(BottomNavigationView::class.java)

        override fun getDescription(): String = "Select bottom navigation item"

        override fun perform(uiController: UiController, view: View) {
            (view as BottomNavigationView).selectedItemId = itemId
            uiController.loopMainThreadUntilIdle()
        }
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

    private fun openCampusTool(cardId: Int) {
        selectBottomTab(R.id.menuFragment)
        onView(withId(cardId)).perform(click())
    }
}
