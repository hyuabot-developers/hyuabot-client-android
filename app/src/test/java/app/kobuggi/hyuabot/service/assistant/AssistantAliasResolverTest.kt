package app.kobuggi.hyuabot.service.assistant

import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaCatalog
import app.kobuggi.hyuabot.service.time.MealPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssistantAliasResolverTest {
    @Test
    fun resolvesStops() {
        assertEquals("dormitory_o", AssistantAliasResolver.resolveStop("기숙사"))
        assertEquals("station", AssistantAliasResolver.resolveStop(" 한대앞 역 "))
        assertEquals("shuttlecock_i", AssistantAliasResolver.resolveStop("셔틀콕 건너편"))
        assertEquals("terminal", AssistantAliasResolver.resolveStop("예술인 정류장"))
        assertNull(AssistantAliasResolver.resolveStop("없는곳"))
    }

    @Test
    fun resolvesDestinations() {
        assertEquals("STATION", AssistantAliasResolver.resolveDestination("한대앞행"))
        assertEquals("JUNGANG", AssistantAliasResolver.resolveDestination("중앙역 방면"))
        assertEquals("CAMPUS", AssistantAliasResolver.resolveDestination("학교"))
        assertNull(AssistantAliasResolver.resolveDestination("부산"))
    }

    @Test
    fun resolvesMeals() {
        assertEquals(MealPeriod.LUNCH, AssistantAliasResolver.resolveMeal("점심"))
        assertEquals(MealPeriod.DINNER, AssistantAliasResolver.resolveMeal("Dinner"))
        assertNull(AssistantAliasResolver.resolveMeal("간식"))
    }

    @Test
    fun resolvesCafeterias() {
        assertEquals(12, AssistantAliasResolver.resolveCafeteria("학생식당", 2))
        assertEquals(12, AssistantAliasResolver.resolveCafeteria("학생 식당", 1))
        assertEquals(13, AssistantAliasResolver.resolveCafeteria("기숙사", 2))
        assertEquals(6, AssistantAliasResolver.resolveCafeteria("기숙사", 1))
        assertEquals(1, AssistantAliasResolver.resolveCafeteria("학생회관", 2))
        assertNull(AssistantAliasResolver.resolveCafeteria("없는식당", 2))
        assertEquals(CafeteriaCatalog.CAMPUS_ERICA, CafeteriaCatalog.bySeq(12)?.campusId)
        assertEquals(R.string.widget_cafeteria_1, CafeteriaCatalog.nameRes(99))
    }
}
