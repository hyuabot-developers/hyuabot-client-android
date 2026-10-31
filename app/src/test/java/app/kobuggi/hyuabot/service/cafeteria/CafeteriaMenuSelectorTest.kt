package app.kobuggi.hyuabot.service.cafeteria

import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.service.time.MealPeriod
import org.junit.Assert.assertEquals
import org.junit.Test

class CafeteriaMenuSelectorTest {
    private val seq12 = CafeteriaPageQuery.Cafeterium(
        __typename = "Cafeterium",
        seq = 12,
        name = "Cafeteria 12",
        latitude = 0.0,
        longitude = 0.0,
        runningTime = CafeteriaPageQuery.RunningTime(
            __typename = "RunningTime",
            breakfast = null,
            lunch = "11:00-14:00",
            dinner = "17:00-19:00",
        ),
        menus = listOf(
            menu("중식", "김치찌개"),
            menu("중식", "김치찌개"),
            menu("석식", "돈까스"),
        ),
    )

    private val seq11 = CafeteriaPageQuery.Cafeterium(
        __typename = "Cafeterium",
        seq = 11,
        name = "Cafeteria 11",
        latitude = 0.0,
        longitude = 0.0,
        runningTime = CafeteriaPageQuery.RunningTime(
            __typename = "RunningTime",
            breakfast = null,
            lunch = null,
            dinner = null,
        ),
        menus = listOf(menu("조식", "토스트")),
    )

    private val cafeterias = listOf(seq12, seq11)

    @Test
    fun servingCafeteriasFiltersByMealAndPreservesOrder() {
        assertEquals(listOf(seq12), CafeteriaMenuSelector.servingCafeterias(cafeterias, MealPeriod.LUNCH))
        assertEquals(listOf(seq11), CafeteriaMenuSelector.servingCafeterias(cafeterias, MealPeriod.BREAKFAST))
        assertEquals(listOf(seq12), CafeteriaMenuSelector.servingCafeterias(cafeterias, MealPeriod.DINNER))
    }

    @Test
    fun menusForDeduplicatesMatchingFood() {
        assertEquals(listOf("김치찌개"), CafeteriaMenuSelector.menusFor(seq12, MealPeriod.LUNCH).map { it.food })
    }

    @Test
    fun runningTimeForReturnsPeriodTime() {
        assertEquals(null, CafeteriaMenuSelector.runningTimeFor(seq12, MealPeriod.BREAKFAST))
        assertEquals("11:00-14:00", CafeteriaMenuSelector.runningTimeFor(seq12, MealPeriod.LUNCH))
        assertEquals("17:00-19:00", CafeteriaMenuSelector.runningTimeFor(seq12, MealPeriod.DINNER))
    }

    private fun menu(type: String, food: String) = CafeteriaPageQuery.Menu(
        __typename = "Menu",
        type = type,
        food = food,
        price = "0",
    )
}
