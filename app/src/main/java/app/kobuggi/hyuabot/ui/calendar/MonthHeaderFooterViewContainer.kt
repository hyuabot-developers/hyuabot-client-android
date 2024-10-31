package app.kobuggi.hyuabot.ui.calendar

import android.view.View
import app.kobuggi.hyuabot.databinding.CalendarHeaderLayoutBinding
import com.kizitonwose.calendar.view.ViewContainer

class MonthHeaderFooterViewContainer(view: View) : ViewContainer(view) {
    val headerTextView = CalendarHeaderLayoutBinding.bind(view).calendarHeader
}
