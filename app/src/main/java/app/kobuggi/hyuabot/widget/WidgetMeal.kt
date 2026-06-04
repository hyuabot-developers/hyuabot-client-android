package app.kobuggi.hyuabot.widget

import app.kobuggi.hyuabot.R
import java.time.LocalTime

enum class WidgetMeal(
    val typeString: String,
    val titleRes: Int,
    val iconRes: Int,
    val tab: String,
) {
    BREAKFAST("조식", R.string.cafeteria_tab_breakfast, R.drawable.ic_meal_breakfast, "breakfast"),
    LUNCH("중식", R.string.cafeteria_tab_lunch, R.drawable.ic_meal_lunch, "lunch"),
    DINNER("석식", R.string.cafeteria_tab_dinner, R.drawable.ic_meal_dinner, "dinner"),
    CLOSED("조식", R.string.widget_meal_closed, R.drawable.ic_meal_closed, "breakfast");

    companion object {
        fun current(time: LocalTime): WidgetMeal = when (time.hour) {
            in 0 until 10 -> BREAKFAST
            in 10 until 15 -> LUNCH
            in 15 until 20 -> DINNER
            else -> CLOSED
        }
    }
}
