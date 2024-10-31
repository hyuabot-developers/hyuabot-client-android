package app.kobuggi.hyuabot.ui.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentCalendarBinding
import app.kobuggi.hyuabot.service.database.entity.Event
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
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
        val eventListAdapter = CalendarEventAdapter(requireContext(), emptyList())

        viewModel.apply {
            fetchCalendarVersion()
            events.observe(viewLifecycleOwner) { eventList ->
                if (eventList.isEmpty()) return@observe
                val currentMonth = YearMonth.now()
                val firstDate = LocalDate.parse(eventList.first().startDate)
                val lastDate = LocalDate.parse(eventList.last().endDate)
                val firstMonth = YearMonth.of(firstDate.year, firstDate.month)
                val lastMonth = YearMonth.of(lastDate.year, lastDate.month)
                val eventMap = countOfEvent(eventList)
                binding.calendarView.apply {
                    setup(firstMonth, lastMonth, WeekFields.of(Locale.getDefault()).firstDayOfWeek)
                    scrollToMonth(currentMonth)
                    dayBinder = object : MonthDayBinder<DayViewContainer> {
                        override fun create(view: View) = DayViewContainer(view)
                        @SuppressLint("SetTextI18n")
                        override fun bind(container: DayViewContainer, day: CalendarDay) {
                            container.dayTextView.apply {
                                text = day.date.dayOfMonth.toString()
                                when (day.position) {
                                    DayPosition.MonthDate -> setTextColor(ResourcesCompat.getColor(resources, R.color.primary_text, null))
                                    else -> setTextColor(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))
                                }
                            }
                            if (day.date in eventMap) {
                                if (eventMap[day.date] == null) return
                                val events = eventMap[day.date]!!
                                if (events.contains("전체")) {
                                    container.otherSchedule.visibility = View.VISIBLE
                                } else if (events.contains("1학년")) {
                                    container.firstSchedule.visibility = View.VISIBLE
                                } else if (events.contains("2학년")) {
                                    container.secondSchedule.visibility = View.VISIBLE
                                } else if (events.contains("3학년")) {
                                    container.thirdSchedule.visibility = View.VISIBLE
                                } else if (events.contains("4학년")) {
                                    container.fourthSchedule.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    monthScrollListener = { month ->
                        eventListAdapter.setEvents(eventList.filter{
                            val startDate = LocalDate.parse(it.startDate)
                            val endDate = LocalDate.parse(it.endDate)
                            startDate <= month.yearMonth.atEndOfMonth() && endDate >= month.yearMonth.atStartOfMonth()
                        }.sortedBy { it.startDate })
                    }
                }
            }
        }

        binding.apply {
            calendarView.apply {
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
            }
            eventListOfMonth.apply {
                adapter = eventListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
        return binding.root
    }

    private fun countOfEvent(events: List<Event>): Map<LocalDate, List<String>> {
        val map = mutableMapOf<LocalDate, List<String>>()
        events.forEach {
            if (it.title.contains("방학")) return@forEach
            val startDate = LocalDate.parse(it.startDate)
            val endDate = LocalDate.parse(it.endDate)
            var date = startDate
            while (date <= endDate) {
                if (map.containsKey(date)) {
                    map[date] = map[date]!!.plus(it.category)
                } else {
                    map[date] = listOf(it.category)
                }
                date = date.plusDays(1)
            }
        }
        return map
    }
}
