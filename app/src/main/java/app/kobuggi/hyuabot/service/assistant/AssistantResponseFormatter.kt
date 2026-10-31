package app.kobuggi.hyuabot.service.assistant

import android.content.Context
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaCatalog
import app.kobuggi.hyuabot.service.time.MealPeriod
import app.kobuggi.hyuabot.widget.ShuttleWidgetSupport
import java.time.format.DateTimeFormatter

class AssistantResponseFormatter(private val context: Context) {
    fun format(answer: CafeteriaMenuAnswer): String = when (answer) {
        is CafeteriaMenuAnswer.Menus -> {
            val entries = answer.cafeterias.joinToString(" / ") { summary ->
                val foods = summary.foods.joinToString(", ")
                if (summary.runningTime.isNullOrBlank()) {
                    context.getString(R.string.assistant_cafeteria_entry, cafeteriaName(summary.seq), foods)
                } else {
                    context.getString(
                        R.string.assistant_cafeteria_entry_with_time,
                        cafeteriaName(summary.seq),
                        summary.runningTime,
                        foods,
                    )
                }
            }
            context.getString(R.string.assistant_cafeteria_menus, mealLabel(answer.meal), entries)
        }
        is CafeteriaMenuAnswer.NoMenu -> if (answer.cafeteriaSeq == null) {
            context.getString(R.string.assistant_cafeteria_no_menu, mealLabel(answer.meal))
        } else {
            context.getString(
                R.string.assistant_cafeteria_no_menu_named,
                cafeteriaName(answer.cafeteriaSeq),
                mealLabel(answer.meal),
            )
        }
        is CafeteriaMenuAnswer.UnknownCafeteria -> context.getString(R.string.assistant_cafeteria_unknown, answer.input)
        CafeteriaMenuAnswer.NetworkError -> context.getString(R.string.assistant_network_error)
    }

    fun format(answer: NextShuttleAnswer): String = when (answer) {
        is NextShuttleAnswer.Departure -> {
            val time = answer.departureTime.format(TIME_FORMAT)
            val base = if (answer.minutesUntil <= 0) {
                context.getString(
                    R.string.assistant_shuttle_next_now,
                    stopName(answer.stopCode),
                    time,
                    destinationName(answer.destinationCode),
                )
            } else {
                context.getString(
                    R.string.assistant_shuttle_next,
                    stopName(answer.stopCode),
                    answer.minutesUntil,
                    time,
                    destinationName(answer.destinationCode),
                )
            }
            answer.followingTime?.let {
                "$base ${context.getString(R.string.assistant_shuttle_following, it.format(TIME_FORMAT))}"
            } ?: base
        }
        is NextShuttleAnswer.NoMoreService -> if (answer.destinationCode == null) {
            context.getString(R.string.assistant_shuttle_ended, stopName(answer.stopCode))
        } else {
            context.getString(
                R.string.assistant_shuttle_ended_destination,
                stopName(answer.stopCode),
                destinationName(answer.destinationCode),
            )
        }
        is NextShuttleAnswer.UnknownStop -> context.getString(R.string.assistant_shuttle_unknown_stop, answer.input)
        is NextShuttleAnswer.UnknownDestination -> context.getString(R.string.assistant_shuttle_unknown_destination, answer.input)
        NextShuttleAnswer.NetworkError -> context.getString(R.string.assistant_network_error)
    }

    fun cafeteriaName(seq: Int): String = context.getString(CafeteriaCatalog.nameRes(seq))

    fun stopName(code: String): String = ShuttleWidgetSupport.stopDisplayName(context, code)

    fun destinationName(code: String): String = ShuttleWidgetSupport.destinationDisplayName(context, code)

    fun mealLabel(meal: MealPeriod): String = context.getString(meal.labelRes)

    private companion object {
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
