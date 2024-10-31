package app.kobuggi.hyuabot.ui.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemCalendarEventBinding
import app.kobuggi.hyuabot.service.database.entity.Event
import java.time.LocalDate

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
}
