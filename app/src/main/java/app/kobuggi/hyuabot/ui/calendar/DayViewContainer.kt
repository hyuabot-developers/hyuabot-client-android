package app.kobuggi.hyuabot.ui.calendar

import android.view.View
import app.kobuggi.hyuabot.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendar.view.ViewContainer

class DayViewContainer(view: View): ViewContainer(view) {
    val dayTextView = CalendarDayLayoutBinding.bind(view).exOneDayText
}
