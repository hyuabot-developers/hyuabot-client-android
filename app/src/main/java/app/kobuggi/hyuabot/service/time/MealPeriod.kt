package app.kobuggi.hyuabot.service.time

import androidx.annotation.StringRes
import app.kobuggi.hyuabot.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

enum class MealPeriod(val typeString: String, @StringRes val labelRes: Int) {
    BREAKFAST("조식", R.string.cafeteria_tab_breakfast),
    LUNCH("중식", R.string.cafeteria_tab_lunch),
    DINNER("석식", R.string.cafeteria_tab_dinner);

    companion object {
        const val NEXT_DAY_FROM_HOUR = 20

        fun current(time: LocalTime): MealPeriod = when (time.hour) {
            in 0 until 10 -> BREAKFAST
            in 10 until 15 -> LUNCH
            else -> DINNER
        }

        fun screenDefault(now: ZonedDateTime): MealSelection {
            val seoul = now.withZoneSameInstant(CampusTime.ZONE)
            return if (seoul.hour >= NEXT_DAY_FROM_HOUR) {
                MealSelection(seoul.toLocalDate().plusDays(1), BREAKFAST, true)
            } else {
                MealSelection(seoul.toLocalDate(), current(seoul.toLocalTime()), false)
            }
        }
    }
}

data class MealSelection(val date: LocalDate, val meal: MealPeriod, val isTomorrow: Boolean)
