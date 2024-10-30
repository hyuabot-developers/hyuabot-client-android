package app.kobuggi.hyuabot.ui.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import dagger.hilt.android.AndroidEntryPoint
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentCalendarBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<CalendarViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.apply {
            fetchCalendarVersion()
            events.observe(viewLifecycleOwner) {
                Log.d("CalendarFragment", it.toString())
            }
        }

        binding.apply {
            calendarView.apply {
                val currentMonth = YearMonth.now()
                val firstMonth = YearMonth.of(currentMonth.year - 1, 1)
                val lastMonth = YearMonth.of(currentMonth.year + 1, 12)
                val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
                monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderFooterViewContainer> {
                    override fun create(view: View) = MonthHeaderFooterViewContainer(view)
                    override fun bind(container: MonthHeaderFooterViewContainer, month: CalendarMonth) {
                        container.headerTextView.text = getString(
                            R.string.month_header,
                            month.yearMonth.year,
                            month.yearMonth.monthValue
                        )
                    }
                }
                dayBinder = object : MonthDayBinder<DayViewContainer> {
                    override fun create(view: View) = DayViewContainer(view)
                    @SuppressLint("SetTextI18n")
                    override fun bind(container: DayViewContainer, day: CalendarDay) {
                        container.dayTextView.text = day.date.dayOfMonth.toString()
                    }
                }
                setup(firstMonth, lastMonth, firstDayOfWeek)
                scrollToMonth(currentMonth)
            }
        }
        return binding.root
    }
}
