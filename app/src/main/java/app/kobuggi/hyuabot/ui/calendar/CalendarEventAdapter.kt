package app.kobuggi.hyuabot.ui.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemCalendarEventBinding
import app.kobuggi.hyuabot.service.database.entity.Event
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CalendarEventAdapter(
    private val context: Context, private var events: List<Event>
): RecyclerView.Adapter<CalendarEventAdapter.CalendarEventViewHolder>() {
    inner class CalendarEventViewHolder(private val binding: ItemCalendarEventBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventName.text = event.title
            val startDate = LocalDate.parse(event.startDate)
            val endDate = LocalDate.parse(event.endDate)
            binding.eventDate.text = context.getString(
                R.string.event_date_range_format,
                startDate.monthValue, startDate.dayOfMonth,
                endDate.monthValue, endDate.dayOfMonth
            )
            binding.eventDescription.apply {
                text = event.description
                visibility = if (event.description.isBlank()) View.GONE else View.VISIBLE
            }
            binding.eventStatus.text = eventStatus(startDate, endDate)
            binding.eventCategoryIndicator.setBackgroundColor(eventColor(event.category))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarEventViewHolder {
        val binding = ItemCalendarEventBinding.inflate(LayoutInflater.from(context), parent, false)
        return CalendarEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarEventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    @SuppressLint("NotifyDataSetChanged")
    fun setEvents(events: List<Event>) {
        this.events = events
        notifyDataSetChanged()
    }

    private fun eventStatus(startDate: LocalDate, endDate: LocalDate): String {
        val today = LocalDate.now()
        return if (today in startDate..endDate) {
            context.getString(R.string.calendar_event_ongoing)
        } else {
            val daysUntilStart = ChronoUnit.DAYS.between(today, startDate)
            if (daysUntilStart >= 0) "D-$daysUntilStart" else "D+${-daysUntilStart}"
        }
    }

    private fun eventColor(category: String): Int {
        return ContextCompat.getColor(
            context,
            when (category) {
                "1학년" -> R.color.hanyang_blue
                "2학년" -> R.color.hanyang_orange
                "3학년" -> R.color.hanyang_green
                "4학년" -> R.color.hanyang_gold
                else -> R.color.hanyang_orange
            }
        )
    }
}
