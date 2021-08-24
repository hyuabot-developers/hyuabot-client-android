package app.kobuggi.hyuabot.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import app.kobuggi.hyuabot.R
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

class CalendarActivity : AppCompatActivity() {
    inner class DayViewContainer(view: View) : ViewContainer(view){
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }

    inner class MonthViewContainer(view: View) : ViewContainer(view){
        val monthHeader: TextView = view.findViewById(R.id.month_header_text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        val calendarView = findViewById<CalendarView>(R.id.calendar_view)
        calendarView.dayBinder = object : DayBinder<DayViewContainer>{
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
            }
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer>{
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                container.monthHeader.text = "${month.year}년 ${month.month}월"
            }

            override fun create(view: View) = MonthViewContainer(view)
        }

        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
    }
}