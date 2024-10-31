package app.kobuggi.hyuabot.ui.calendar

import android.view.View
import app.kobuggi.hyuabot.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendar.view.ViewContainer

class DayViewContainer(view: View): ViewContainer(view) {
    val dayItem = CalendarDayLayoutBinding.bind(view).calendarDayItem
    val dayTextView = CalendarDayLayoutBinding.bind(view).calendarDayText
    val firstSchedule = CalendarDayLayoutBinding.bind(view).calendarDayScheduleFirst
    val secondSchedule = CalendarDayLayoutBinding.bind(view).calendarDayScheduleSecond
    val thirdSchedule = CalendarDayLayoutBinding.bind(view).calendarDayScheduleThird
    val fourthSchedule = CalendarDayLayoutBinding.bind(view).calendarDayScheduleFourth
    val otherSchedule = CalendarDayLayoutBinding.bind(view).calendarDayScheduleOther
}
