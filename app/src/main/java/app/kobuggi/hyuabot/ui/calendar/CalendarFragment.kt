package app.kobuggi.hyuabot.ui.calendar

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentCalendarBinding
import app.kobuggi.hyuabot.service.database.CalendarDatabaseItem
import com.google.firebase.analytics.FirebaseAnalytics
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@AndroidEntryPoint
class CalendarFragment : Fragment(), DialogInterface.OnDismissListener {
    private val vm by viewModels<CalendarViewModel>()
    private lateinit var binding: FragmentCalendarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = vm
        binding.calendarView.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                container.headerTextView.text = requireContext().getString(
                    R.string.month_header,
                    data.yearMonth.year,
                    data.yearMonth.monthValue
                )
            }
        }
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.dayTextView.text = data.date.dayOfMonth.toString()
                if (data.position == DayPosition.MonthDate){
                    container.dayTextView.setTextColor(ResourcesCompat.getColor(resources, R.color.primaryTextColor, null))
                    val eventsOfDay = countScheduleOfDay(data)
                    container.firstSchedule.visibility = if (eventsOfDay.containsKey(1)) View.VISIBLE else View.INVISIBLE
                    container.secondSchedule.visibility = if (eventsOfDay.containsKey(2)) View.VISIBLE else View.INVISIBLE
                    container.thirdSchedule.visibility = if (eventsOfDay.containsKey(3)) View.VISIBLE else View.INVISIBLE
                    container.fourthSchedule.visibility = if (eventsOfDay.containsKey(4)) View.VISIBLE else View.INVISIBLE
                    container.otherSchedule.visibility = if (eventsOfDay.keys.any { it < 1 }) View.VISIBLE else View.INVISIBLE

                    container.dayItem.setOnClickListener {
                        vm.clickedDate.value = data
                        vm.showSchedule.value = eventsOfDay
                        vm.showDaySchedule.value = true
                    }

                } else {
                    container.dayTextView.setTextColor(Color.GRAY)
                }
            }
        }
        binding.calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(currentMonth: CalendarMonth) {
                vm.onCalendarMonthChanged(currentMonth.yearMonth)
            }
        }

        val eventAdapter = CalendarEventAdapter(requireContext(), arrayListOf(),
            { run {  } },
            { previousPosition: Int, currentPosition: Int -> setSelectedItem(previousPosition, currentPosition) }
        )
        binding.eventListOfMonth.adapter = eventAdapter
        binding.eventListOfMonth.layoutManager = LinearLayoutManager(requireContext())
        vm.eventsOfMonth.observe(viewLifecycleOwner) {
            eventAdapter.setEvents(it)
            binding.calendarView.notifyMonthChanged(vm.currentMonthData.value!!)
        }

        val currentMonth = YearMonth.now()
        val firstMonth = YearMonth.of(currentMonth.year - 1, 1)
        val lastMonth = YearMonth.of(currentMonth.year + 1, 12)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        binding.calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner, resources.getStringArray(R.array.target_grade))
        binding.targetGradeSpinner.adapter = spinnerAdapter
        binding.targetGradeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
            }
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                vm.filterByGrade(position)
            }
        }
        vm.showDaySchedule.observe(viewLifecycleOwner) {
            if (it){
                val dialog = CalendarDayDialog()
                dialog.show(childFragmentManager, "dialog")
                vm.showDaySchedule.value = false
            }
        }
        return binding.root
    }

    private fun setSelectedItem(previousPosition: Int, currentPosition: Int) {
        if(previousPosition != -1) {
            binding.eventListOfMonth.findViewHolderForAdapterPosition(previousPosition)?.itemView!!.findViewById<TextView>(R.id.event_title).isSelected = false
        }
        binding.eventListOfMonth.findViewHolderForAdapterPosition(currentPosition)?.itemView!!.findViewById<TextView>(R.id.event_title).isSelected = true
    }

    private fun countScheduleOfDay(day: CalendarDay): Map<Int, List<CalendarDatabaseItem>> {
        val startOfDay = LocalDateTime.of(day.date, LocalDateTime.MIN.toLocalTime())
        val endOfDay = LocalDateTime.of(day.date, LocalDateTime.MAX.toLocalTime())
        return vm.eventsOfMonth.value?.filter {
            (LocalDateTime.parse(it.startDate!!.split("+")[0]) <= startOfDay && LocalDateTime.parse(it.endDate!!.split("+")[0]) > startOfDay) ||
            (LocalDateTime.parse(it.startDate.split("+")[0]) in startOfDay..endOfDay)
        }?.groupBy { it.targetGrade!! } ?: mapOf()
    }

    override fun onDismiss(dialogInterface: DialogInterface) {
        vm.showDaySchedule.value = false
    }

    override fun onResume() {
        super.onResume()
        vm.showDaySchedule.value = false
        if (activity is MainActivity) {
            (activity as MainActivity).getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "학사력 목록")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "CalendarFragment")
            })
        }
    }
}