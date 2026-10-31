package app.kobuggi.hyuabot.service.assistant

import app.kobuggi.hyuabot.service.cafeteria.CafeteriaCatalog
import app.kobuggi.hyuabot.service.time.MealPeriod
import java.util.Locale

object AssistantAliasResolver {
    const val DEFAULT_STOP = "dormitory_o"

    fun normalize(input: String): String = input.trim().lowercase(Locale.ROOT).filterNot { it.isWhitespace() }

    fun resolveStop(input: String): String? {
        val value = stripSuffix(normalize(input), listOf("정류장", "정류소", "에서"))
        return stopAliases[value]
    }

    fun resolveDestination(input: String): String? {
        val value = stripSuffix(normalize(input), listOf("방면", "행", "으로", "로"))
        return destinationAliases[value]
    }

    fun resolveMeal(input: String): MealPeriod? = when (normalize(input)) {
        "breakfast", "morning", "아침", "조식", "아침밥" -> MealPeriod.BREAKFAST
        "lunch", "점심", "중식", "점심밥" -> MealPeriod.LUNCH
        "dinner", "저녁", "석식", "저녁밥" -> MealPeriod.DINNER
        else -> null
    }

    fun resolveCafeteria(input: String, campusId: Int): Int? {
        val value = normalize(input)
        if (value in dormitoryCafeteriaAliases) {
            return if (campusId == CafeteriaCatalog.CAMPUS_SEOUL) 6 else 13
        }
        return CafeteriaCatalog.all.firstOrNull { it.aliases.any { alias -> normalize(alias) == value } }?.seq
            ?: value.toIntOrNull()?.takeIf { seq -> CafeteriaCatalog.bySeq(seq) != null }
    }

    private fun stripSuffix(value: String, suffixes: List<String>): String {
        val suffix = suffixes.firstOrNull { value.endsWith(it) }
        return if (suffix == null) value else value.removeSuffix(suffix)
    }

    private val stopAliases = mapOf(
        "dormitory_o" to "dormitory_o",
        "dormitory_i" to "dormitory_o",
        "dormitory" to "dormitory_o",
        "기숙사" to "dormitory_o",
        "기숙사앞" to "dormitory_o",
        "생활관" to "dormitory_o",
        "창의인재원" to "dormitory_o",
        "shuttlecock_o" to "shuttlecock_o",
        "shuttlecock" to "shuttlecock_o",
        "셔틀콕" to "shuttlecock_o",
        "셔틀콕앞" to "shuttlecock_o",
        "shuttlecock_i" to "shuttlecock_i",
        "셔틀콕건너편" to "shuttlecock_i",
        "셔틀콕반대편" to "shuttlecock_i",
        "station" to "station",
        "한대앞" to "station",
        "한대앞역" to "station",
        "한양대앞" to "station",
        "한양대앞역" to "station",
        "역" to "station",
        "terminal" to "terminal",
        "예술인" to "terminal",
        "예술인a" to "terminal",
        "터미널" to "terminal",
        "버스터미널" to "terminal",
        "jungang_stn" to "jungang_stn",
        "jungang" to "jungang_stn",
        "중앙역" to "jungang_stn",
        "중앙" to "jungang_stn",
    )

    private val destinationAliases = mapOf(
        "station" to "STATION",
        "한대앞" to "STATION",
        "한대앞역" to "STATION",
        "한양대앞" to "STATION",
        "한양대앞역" to "STATION",
        "역" to "STATION",
        "terminal" to "TERMINAL",
        "예술인" to "TERMINAL",
        "터미널" to "TERMINAL",
        "버스터미널" to "TERMINAL",
        "jungang" to "JUNGANG",
        "중앙역" to "JUNGANG",
        "중앙" to "JUNGANG",
        "campus" to "CAMPUS",
        "학교" to "CAMPUS",
        "캠퍼스" to "CAMPUS",
        "기숙사" to "CAMPUS",
        "셔틀콕" to "CAMPUS",
        "dormitory" to "CAMPUS",
    )

    private val dormitoryCafeteriaAliases = setOf("기숙사", "생활관", "기숙사식당", "생활관식당")
}
