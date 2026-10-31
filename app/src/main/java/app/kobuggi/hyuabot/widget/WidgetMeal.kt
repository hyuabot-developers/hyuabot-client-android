package app.kobuggi.hyuabot.widget

import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.time.MealPeriod
import java.time.LocalTime

enum class WidgetMeal(
    val typeString: String,
    val titleRes: Int,
    val iconRes: Int,
    val tab: String,
    val mealPeriod: MealPeriod,
) {
    BREAKFAST("조식", R.string.cafeteria_tab_breakfast, R.drawable.ic_meal_breakfast, "breakfast", MealPeriod.BREAKFAST),
    LUNCH("중식", R.string.cafeteria_tab_lunch, R.drawable.ic_meal_lunch, "lunch", MealPeriod.LUNCH),
    DINNER("석식", R.string.cafeteria_tab_dinner, R.drawable.ic_meal_dinner, "dinner", MealPeriod.DINNER),
    CLOSED("조식", R.string.widget_meal_closed, R.drawable.ic_meal_closed, "breakfast", MealPeriod.BREAKFAST);

    companion object {
        fun current(time: LocalTime): WidgetMeal =
            if (time.hour >= 20) CLOSED else when (MealPeriod.current(time)) {
                MealPeriod.BREAKFAST -> BREAKFAST
                MealPeriod.LUNCH -> LUNCH
                MealPeriod.DINNER -> DINNER
            }
    }
}
