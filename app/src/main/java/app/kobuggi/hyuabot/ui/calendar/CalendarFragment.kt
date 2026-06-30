package app.kobuggi.hyuabot.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentCalendarBinding
import app.kobuggi.hyuabot.service.database.entity.Event
import app.kobuggi.hyuabot.util.setSkeletonLoading
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentCalendarBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<CalendarViewModel>()
    private var events: List<Event> = emptyList()
    private var firstMonth: YearMonth = YearMonth.now()
    private var lastMonth: YearMonth = YearMonth.now()
    private var displayedMonth: YearMonth = YearMonth.now()
    private var selectedDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val eventListAdapter = CalendarEventAdapter(requireContext(), emptyList())
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        viewModel.apply {
            fetchCalendarVersion()
            updating.observe(viewLifecycleOwner) { updating ->
                binding.loadingLayout.setSkeletonLoading(updating)
            }
            queryError.observe(viewLifecycleOwner) {
                it?.let { Toast.makeText(requireContext(), getString(R.string.calendar_error), Toast.LENGTH_SHORT).show() }
            }
            events.observe(viewLifecycleOwner) { eventList ->
                if (eventList.isEmpty()) return@observe
                this@CalendarFragment.events = eventList
                val firstDate = LocalDate.parse(eventList.first().startDate)
                val lastDate = LocalDate.parse(eventList.last().endDate)
                firstMonth = YearMonth.from(firstDate)
                lastMonth = YearMonth.from(lastDate)
                displayedMonth = YearMonth.now().coerceIn(firstMonth, lastMonth)
                selectedDate = null
                binding.calendarTimelineView.setEvents(eventList)
                updateCalendar(eventListAdapter)
            }
        }

        binding.apply {
            calendarTimelineView.apply {
                setFirstDayOfWeek(firstDayOfWeek)
                onDateSelected = { date ->
                    selectedDate = date
                    updateSelectedDateEvents(eventListAdapter)
                }
            }
            previousMonthButton.setOnClickListener {
                if (displayedMonth > firstMonth) {
                    displayedMonth = displayedMonth.minusMonths(1)
                    selectedDate = null
                    calendarTimelineView.clearSelection()
                    updateCalendar(eventListAdapter)
                }
            }
            nextMonthButton.setOnClickListener {
                if (displayedMonth < lastMonth) {
                    displayedMonth = displayedMonth.plusMonths(1)
                    selectedDate = null
                    calendarTimelineView.clearSelection()
                    updateCalendar(eventListAdapter)
                }
            }
            bindWeekHeader(firstDayOfWeek)
            eventListOfMonth.apply {
                adapter = eventListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
        return binding.root
    }

    private fun updateCalendar(eventListAdapter: CalendarEventAdapter) {
        binding.monthTitle.text = getString(
            R.string.month_header,
            displayedMonth.year,
            displayedMonth.monthValue
        )
        binding.calendarTimelineView.setMonth(displayedMonth)
        binding.previousMonthButton.isEnabled = displayedMonth > firstMonth
        binding.nextMonthButton.isEnabled = displayedMonth < lastMonth
        binding.previousMonthButton.alpha = if (displayedMonth > firstMonth) 1f else 0.35f
        binding.nextMonthButton.alpha = if (displayedMonth < lastMonth) 1f else 0.35f
        updateSelectedDateEvents(eventListAdapter)
    }

    private fun updateSelectedDateEvents(eventListAdapter: CalendarEventAdapter) {
        val date = selectedDate
        val selectedEvents = if (date == null) {
            emptyList()
        } else {
            events.filter { event ->
                val startDate = LocalDate.parse(event.startDate)
                val endDate = LocalDate.parse(event.endDate)
                date in startDate..endDate
            }.sortedBy { it.startDate }
        }

        binding.calendarSelectDateHint.visibility =
            if (date == null || selectedEvents.isEmpty()) View.VISIBLE else View.GONE
        binding.eventListOfMonth.visibility =
            if (date != null && selectedEvents.isNotEmpty()) View.VISIBLE else View.GONE
        eventListAdapter.setEvents(selectedEvents)
    }

    private fun bindWeekHeader(firstDayOfWeek: DayOfWeek) {
        val weekDayViews = listOf(
            binding.weekDay0,
            binding.weekDay1,
            binding.weekDay2,
            binding.weekDay3,
            binding.weekDay4,
            binding.weekDay5,
            binding.weekDay6
        )
        val weekDayNames = mapOf(
            DayOfWeek.SUNDAY to R.string.sunday,
            DayOfWeek.MONDAY to R.string.monday,
            DayOfWeek.TUESDAY to R.string.tuesday,
            DayOfWeek.WEDNESDAY to R.string.wednesday,
            DayOfWeek.THURSDAY to R.string.thursday,
            DayOfWeek.FRIDAY to R.string.friday,
            DayOfWeek.SATURDAY to R.string.saturday
        )

        weekDayViews.forEachIndexed { index, textView ->
            val day = firstDayOfWeek.plus(index.toLong())
            textView.text = getString(weekDayNames.getValue(day))
            textView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when (day) {
                        DayOfWeek.SUNDAY -> R.color.calendar_sunday
                        DayOfWeek.SATURDAY -> R.color.calendar_saturday
                        else -> R.color.primary_text
                    }
                )
            )
        }
    }
}
