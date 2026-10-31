package app.kobuggi.hyuabot.service.assistant

import app.kobuggi.hyuabot.service.cafeteria.CafeteriaCatalog
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaMenuSelector
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaRepository
import app.kobuggi.hyuabot.service.time.CampusTime
import app.kobuggi.hyuabot.service.time.MealPeriod
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import java.time.LocalDate
import java.time.ZonedDateTime
import com.apollographql.cache.normalized.FetchPolicy
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

data class CafeteriaMealSummary(val seq: Int, val runningTime: String?, val foods: List<String>)

sealed interface CafeteriaMenuAnswer {
    data class Menus(val date: LocalDate, val meal: MealPeriod, val cafeterias: List<CafeteriaMealSummary>) : CafeteriaMenuAnswer
    data class NoMenu(val date: LocalDate, val meal: MealPeriod, val cafeteriaSeq: Int?) : CafeteriaMenuAnswer
    data class UnknownCafeteria(val input: String) : CafeteriaMenuAnswer
    data object NetworkError : CafeteriaMenuAnswer
}

class GetTodayCafeteriaMenuUseCase @Inject constructor(
    private val cafeteriaRepository: CafeteriaRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(cafeteria: String?, meal: String?, now: ZonedDateTime = CampusTime.now()): CafeteriaMenuAnswer {
        val mealPeriod = meal?.takeIf { it.isNotBlank() }?.let(AssistantAliasResolver::resolveMeal)
            ?: MealPeriod.current(now.toLocalTime())
        val preferredCampus = userPreferencesRepository.campusID.first()
        val targetSeq = if (cafeteria.isNullOrBlank()) null else
            AssistantAliasResolver.resolveCafeteria(cafeteria, preferredCampus)
                ?: return CafeteriaMenuAnswer.UnknownCafeteria(cafeteria.trim())
        val campusId = targetSeq?.let { CafeteriaCatalog.bySeq(it)?.campusId } ?: preferredCampus
        val date = now.toLocalDate()
        val list = withTimeoutOrNull(TIMEOUT_MS) {
            cafeteriaRepository.fetchCafeterias(date, campusId, FetchPolicy.NetworkFirst)
        }?.getOrNull() ?: return CafeteriaMenuAnswer.NetworkError
        val serving = CafeteriaMenuSelector.servingCafeterias(list, mealPeriod)
            .filter { targetSeq == null || it.seq == targetSeq }
            .sortedBy { it.seq }
        val summaries = serving.map {
            CafeteriaMealSummary(
                it.seq,
                CafeteriaMenuSelector.runningTimeFor(it, mealPeriod),
                CafeteriaMenuSelector.menusFor(it, mealPeriod).map { menu -> menu.food },
            )
        }.filter { it.foods.isNotEmpty() }
        return if (summaries.isEmpty()) CafeteriaMenuAnswer.NoMenu(date, mealPeriod, targetSeq)
        else CafeteriaMenuAnswer.Menus(date, mealPeriod, summaries)
    }

    companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
