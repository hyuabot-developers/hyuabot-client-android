package app.kobuggi.hyuabot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.model.Events
import app.kobuggi.hyuabot.model.MenuItem
import app.kobuggi.hyuabot.model.Restaurant
import app.kobuggi.hyuabot.model.RestaurantList
import java.time.LocalDateTime

class EventsCardListAdapter(list: ArrayList<Events>) : RecyclerView.Adapter<EventsCardListAdapter.ItemViewHolder>(){
    private val mList = list
    private val diffUtil = AsyncListDiffer(this, DateDiffUtilCallback())

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val eventDate = itemView!!.findViewById<TextView>(R.id.calendar_card_date)
        private val eventContents = itemView!!.findViewById<TextView>(R.id.calendar_card_contents)

        @SuppressLint("SetTextI18n")
        fun bind(item: Events){
            eventDate.text = "${item.startDate} ~ ${item.endDate}"
            eventContents.text = item.title
        }

    }

    inner class DateDiffUtilCallback : DiffUtil.ItemCallback<Events>() {
        override fun areItemsTheSame(oldItem: Events, newItem: Events) =
            oldItem.title == newItem.title && oldItem.startDate == newItem.startDate && oldItem.endDate == newItem.endDate

        override fun areContentsTheSame(oldItem: Events, newItem: Events) =
            oldItem.title == newItem.title && oldItem.startDate == newItem.startDate && oldItem.endDate == newItem.endDate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_events, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun replaceTo(events: ArrayList<Events>) = diffUtil.submitList(events)
}