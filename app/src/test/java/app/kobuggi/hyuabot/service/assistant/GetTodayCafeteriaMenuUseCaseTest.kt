package app.kobuggi.hyuabot.service.assistant

import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaRepository
import app.kobuggi.hyuabot.service.time.CampusTime
import app.kobuggi.hyuabot.service.time.MealPeriod
import app.kobuggi.hyuabot.test.UserPreferencesRepositoryTestFactory
import com.apollographql.cache.normalized.FetchPolicy
import java.time.ZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GetTodayCafeteriaMenuUseCaseTest {
    private lateinit var repositoryFactory: UserPreferencesRepositoryTestFactory

    @After
    fun tearDown() {
        repositoryFactory.cleanUp()
    }

    @Test
    fun defaultsToPreferredCampusAndSortsMenus() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val preferences = repositoryFactory.create()
        val fake = FakeCafeteriaRepository(Result.success(listOf(cafeteria(12), cafeteria(11))))
        val now = ZonedDateTime.of(2026, 10, 31, 12, 30, 0, 0, CampusTime.ZONE)

        val answer = GetTodayCafeteriaMenuUseCase(fake, preferences)(null, null, now)

        assertEquals(MealPeriod.LUNCH, (answer as CafeteriaMenuAnswer.Menus).meal)
        assertEquals(listOf(11, 12), answer.cafeterias.map { it.seq })
        assertEquals(2, fake.lastCampusId)
        assertEquals(now.toLocalDate(), fake.lastDate)
    }

    @Test
    fun cafeteriaAliasRestrictsResults() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val preferences = repositoryFactory.create()
        val fake = FakeCafeteriaRepository(Result.success(listOf(cafeteria(11), cafeteria(12))))

        val answer = GetTodayCafeteriaMenuUseCase(fake, preferences)("학생식당", "점심", testNow())

        assertEquals(listOf(12), (answer as CafeteriaMenuAnswer.Menus).cafeterias.map { it.seq })
    }

    @Test
    fun seoulCafeteriaUsesItsOwnCampus() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val preferences = repositoryFactory.create()
        val fake = FakeCafeteriaRepository(Result.success(listOf(cafeteria(1))))

        GetTodayCafeteriaMenuUseCase(fake, preferences)("학생회관", "점심", testNow())

        assertEquals(1, fake.lastCampusId)
    }

    @Test
    fun resolvesDinnerAlias() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val preferences = repositoryFactory.create()
        val fake = FakeCafeteriaRepository(Result.success(listOf(cafeteria(11, "석식"))))
        val morning = ZonedDateTime.of(2026, 10, 31, 8, 0, 0, 0, CampusTime.ZONE)

        assertEquals(MealPeriod.DINNER, (GetTodayCafeteriaMenuUseCase(fake, preferences)(null, "저녁", morning) as CafeteriaMenuAnswer.Menus).meal)
    }

    @Test
    fun emptyListReturnsNoMenu() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val preferences = repositoryFactory.create()
        val date = testNow().toLocalDate()

        assertEquals(CafeteriaMenuAnswer.NoMenu(date, MealPeriod.LUNCH, null), GetTodayCafeteriaMenuUseCase(FakeCafeteriaRepository(Result.success(emptyList())), preferences)(null, null, testNow()))
    }

    @Test
    fun unknownCafeteriaDoesNotCallRepository() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val preferences = repositoryFactory.create()
        val fake = FakeCafeteriaRepository(Result.success(emptyList()))

        assertEquals(CafeteriaMenuAnswer.UnknownCafeteria("없는식당"), GetTodayCafeteriaMenuUseCase(fake, preferences)("없는식당", null, testNow()))
        assertFalse(fake.called)
    }

    @Test
    fun failedRequestReturnsNetworkError() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val preferences = repositoryFactory.create()

        assertEquals(CafeteriaMenuAnswer.NetworkError, GetTodayCafeteriaMenuUseCase(FakeCafeteriaRepository(Result.failure(IllegalStateException())), preferences)(null, null, testNow()))
    }

    @Test
    fun timeoutReturnsNetworkError() = runTest {
        repositoryFactory = UserPreferencesRepositoryTestFactory(this)
        val preferences = repositoryFactory.create()

        assertEquals(CafeteriaMenuAnswer.NetworkError, GetTodayCafeteriaMenuUseCase(FakeCafeteriaRepository(Result.success(emptyList()), 20_000), preferences)(null, null, testNow()))
    }

    private fun testNow() = ZonedDateTime.of(2026, 10, 31, 12, 30, 0, 0, CampusTime.ZONE)

    private fun cafeteria(seq: Int, menuType: String = "중식") = CafeteriaPageQuery.Cafeterium(
        __typename = "Cafeterium",
        seq = seq,
        name = "Cafeteria $seq",
        latitude = 0.0,
        longitude = 0.0,
        runningTime = CafeteriaPageQuery.RunningTime(__typename = "RunningTime", breakfast = null, lunch = "11:00-14:00", dinner = "17:00-19:00"),
        menus = listOf(CafeteriaPageQuery.Menu(__typename = "Menu", type = menuType, food = "김치찌개", price = "0")),
    )

    private class FakeCafeteriaRepository(
        private val result: Result<List<CafeteriaPageQuery.Cafeterium>>,
        private val delayMs: Long = 0,
    ) : CafeteriaRepository {
        var lastDate: java.time.LocalDate? = null
        var lastCampusId: Int? = null
        var called = false

        override suspend fun fetchCafeterias(date: java.time.LocalDate, campusId: Int, fetchPolicy: FetchPolicy): Result<List<CafeteriaPageQuery.Cafeterium>> {
            called = true
            lastDate = date
            lastCampusId = campusId
            delay(delayMs)
            return result
        }
    }

}
