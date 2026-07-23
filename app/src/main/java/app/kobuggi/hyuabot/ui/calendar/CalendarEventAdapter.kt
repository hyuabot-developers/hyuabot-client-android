package app.kobuggi.hyuabot.ui.calendar

import android.annotation.SuppressLint
import android.content.res.ColorStateList
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
            val today = LocalDate.now()
            val isPast = endDate < today
            binding.eventName.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isPast) R.color.tertiary_text else R.color.primary_text
                )
            )
            binding.eventDate.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isPast) R.color.tertiary_text else R.color.secondary_text
                )
            )
            binding.eventStatus.apply {
                val daysUntilStart = ChronoUnit.DAYS.between(today, startDate)
                when {
                    today in startDate..endDate -> {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.calendar_event_ongoing)
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.calendar_status_ongoing)
                        )
                    }
                    daysUntilStart in 0..7 -> {
                        visibility = View.VISIBLE
                        text = "D-$daysUntilStart"
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.calendar_status_upcoming)
                        )
                    }
                    else -> visibility = View.GONE
                }
            }
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

    private fun eventColor(category: String): Int {
        return ContextCompat.getColor(
            context,
            when (category) {
                "1학년" -> R.color.calendar_category_blue
                "2학년" -> R.color.calendar_category_orange
                "3학년" -> R.color.calendar_category_green
                "4학년" -> R.color.calendar_category_purple
                else -> R.color.calendar_category_orange
            }
        )
    }
}
