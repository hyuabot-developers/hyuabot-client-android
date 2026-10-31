package app.kobuggi.hyuabot.ui.cafeteria

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaMenuSelector
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaRepository
import app.kobuggi.hyuabot.service.time.CampusTime
import app.kobuggi.hyuabot.service.time.MealPeriod
import app.kobuggi.hyuabot.test.MainDispatcherRule
import app.kobuggi.hyuabot.test.UserPreferencesRepositoryTestFactory
import app.kobuggi.hyuabot.util.QueryError
import java.io.IOException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.TimeZone
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CafeteriaViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repositoryFactory: UserPreferencesRepositoryTestFactory
    private lateinit var fakeRepository: FakeCafeteriaRepository
    private lateinit var viewModel: CafeteriaViewModel
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        fakeRepository = FakeCafeteriaRepository()
    }

    @After
    fun tearDown() {
        if (::repositoryFactory.isInitialized) repositoryFactory.cleanUp()
        if (::originalTimeZone.isInitialized) TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun initialDateUsesSeoulTime() = runTest {
        createViewModel(this)
        val minutes = ChronoUnit.MINUTES.between(
            viewModel.date.value!!,
            CampusTime.now().toLocalDateTime(),
        )

        assertTrue(kotlin.math.abs(minutes) <= 1)
    }

    @Test
    fun fetchDataPassesDateAndCampusId() = runTest {
        createViewModel(this)
        viewModel.fetchData(2)
        advanceUntilIdle()

        assertEquals(listOf(viewModel.date.value!!.toLocalDate() to 2), fakeRepository.calls)
    }

    @Test
    fun successSplitsMealsAndClearsLoadingAndError() = runTest {
        createViewModel(this)
        val cafeterias = listOf(cafeteria("중식", "김치찌개"))
        fakeRepository.result = Result.success(cafeterias)

        viewModel.fetchData(2)
        advanceUntilIdle()

        assertEquals(CafeteriaMenuSelector.servingCafeterias(cafeterias, MealPeriod.BREAKFAST), viewModel.breakfast.value)
        assertEquals(CafeteriaMenuSelector.servingCafeterias(cafeterias, MealPeriod.LUNCH), viewModel.lunch.value)
        assertEquals(CafeteriaMenuSelector.servingCafeterias(cafeterias, MealPeriod.DINNER), viewModel.dinner.value)
        assertNull(viewModel.queryError.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun successfulRequestIsDeduplicated() = runTest {
        createViewModel(this)
        fakeRepository.result = Result.success(listOf(cafeteria("중식", "김치찌개")))

        viewModel.fetchData(2)
        advanceUntilIdle()
        viewModel.fetchData(2)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.calls.size)
    }

    @Test
    fun failureMapsToServerErrorAndClearsLoading() = runTest {
        createViewModel(this)
        fakeRepository.result = Result.failure(IOException())

        viewModel.fetchData(2)
        advanceUntilIdle()

        assertEquals(QueryError.SERVER_ERROR, viewModel.queryError.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    private fun createViewModel(scope: kotlinx.coroutines.test.TestScope) {
        repositoryFactory = UserPreferencesRepositoryTestFactory(scope)
        viewModel = CafeteriaViewModel(fakeRepository, repositoryFactory.create())
    }

    private fun cafeteria(type: String, food: String) = CafeteriaPageQuery.Cafeterium(
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
            CafeteriaPageQuery.Menu(__typename = "Menu", type = type, food = food, price = "0"),
        ),
    )

    private class FakeCafeteriaRepository : CafeteriaRepository {
        var result: Result<List<CafeteriaPageQuery.Cafeterium>> = Result.success(emptyList())
        val calls = mutableListOf<Pair<LocalDate, Int>>()

        override suspend fun fetchCafeterias(
            date: LocalDate,
            campusId: Int,
            fetchPolicy: com.apollographql.cache.normalized.FetchPolicy,
        ): Result<List<CafeteriaPageQuery.Cafeterium>> {
            calls += date to campusId
            return result
        }
    }
}
