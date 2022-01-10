package app.kobuggi.hyuabot.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.activity.ShuttleActivity
import app.kobuggi.hyuabot.activity.ShuttleTimetableActivity
import app.kobuggi.hyuabot.model.BusTimeTableItem
import app.kobuggi.hyuabot.model.ShuttleCardItem
import app.kobuggi.hyuabot.model.ShuttleItem
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class BusTimetableCardListAdapter(private val list: List<BusTimeTableItem>, private val mContext: Context) : RecyclerView.Adapter<BusTimetableCardListAdapter.ItemViewHolder>(){
    private val now = LocalTime.now()
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val busCardTime = itemView!!.findViewById<TextView>(R.id.bus_timetable_time)


        fun bind(item: BusTimeTableItem){
            busCardTime.text = item.time
            if(now.isAfter(LocalTime.parse(item.time, formatter))){
                busCardTime.setTextColor(Color.GRAY)
            } else {
                busCardTime.setTextColor(mContext.getColor(R.color.primaryTextColor))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_bus_timetable, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}