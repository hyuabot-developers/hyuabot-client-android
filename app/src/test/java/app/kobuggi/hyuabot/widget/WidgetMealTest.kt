package app.kobuggi.hyuabot.widget

import app.kobuggi.hyuabot.service.time.MealPeriod
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetMealTest {
    @Test
    fun currentReturnsBreakfastBeforeTen() {
        assertEquals(WidgetMeal.BREAKFAST, WidgetMeal.current(LocalTime.of(0, 0)))
        assertEquals(WidgetMeal.BREAKFAST, WidgetMeal.current(LocalTime.of(9, 59)))
    }

    @Test
    fun currentReturnsLunchFromTenToBeforeFifteen() {
        assertEquals(WidgetMeal.LUNCH, WidgetMeal.current(LocalTime.of(10, 0)))
        assertEquals(WidgetMeal.LUNCH, WidgetMeal.current(LocalTime.of(14, 59)))
    }

    @Test
    fun currentReturnsDinnerFromFifteenToBeforeTwenty() {
        assertEquals(WidgetMeal.DINNER, WidgetMeal.current(LocalTime.of(15, 0)))
        assertEquals(WidgetMeal.DINNER, WidgetMeal.current(LocalTime.of(19, 59)))
    }

    @Test
    fun currentReturnsClosedFromTwenty() {
        assertEquals(WidgetMeal.CLOSED, WidgetMeal.current(LocalTime.of(20, 0)))
        assertEquals(WidgetMeal.CLOSED, WidgetMeal.current(LocalTime.of(23, 59)))
    }
    @Test
    fun mealPeriodsMatchWidgetEntries() {
        assertEquals(MealPeriod.BREAKFAST, WidgetMeal.BREAKFAST.mealPeriod)
        assertEquals(MealPeriod.LUNCH, WidgetMeal.LUNCH.mealPeriod)
        assertEquals(MealPeriod.DINNER, WidgetMeal.DINNER.mealPeriod)
        assertEquals(MealPeriod.BREAKFAST, WidgetMeal.CLOSED.mealPeriod)
        WidgetMeal.values().forEach { meal ->
            assertEquals(meal.mealPeriod.typeString, meal.typeString)
        }
    }
}
