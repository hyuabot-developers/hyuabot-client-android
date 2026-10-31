package app.kobuggi.hyuabot.service.cafeteria

import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.service.time.MealPeriod

object CafeteriaMenuSelector {
    fun servingCafeterias(
        cafeterias: List<CafeteriaPageQuery.Cafeterium>,
        meal: MealPeriod,
    ): List<CafeteriaPageQuery.Cafeterium> =
        cafeterias.filter { cafeteria -> cafeteria.menus.any { it.type.contains(meal.typeString) } }

    fun menusFor(
        cafeteria: CafeteriaPageQuery.Cafeterium,
        meal: MealPeriod,
    ): List<CafeteriaPageQuery.Menu> =
        cafeteria.menus.filter { it.type.contains(meal.typeString) }.distinctBy { it.food }

    fun runningTimeFor(
        cafeteria: CafeteriaPageQuery.Cafeterium,
        meal: MealPeriod,
    ): String? = when (meal) {
        MealPeriod.BREAKFAST -> cafeteria.runningTime.breakfast
        MealPeriod.LUNCH -> cafeteria.runningTime.lunch
        MealPeriod.DINNER -> cafeteria.runningTime.dinner
    }
}
