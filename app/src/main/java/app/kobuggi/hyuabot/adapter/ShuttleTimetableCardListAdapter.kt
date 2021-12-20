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
import app.kobuggi.hyuabot.model.ShuttleCardItem
import app.kobuggi.hyuabot.model.ShuttleItem
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class ShuttleTimetableCardListAdapter(private val list: List<ShuttleItem>, private val mContext: Context) : RecyclerView.Adapter<ShuttleTimetableCardListAdapter.ItemViewHolder>(){
    private val now = LocalTime.now()
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val shuttleCardHeading = itemView!!.findViewById<TextView>(R.id.shuttle_timetable_heading)
        private val shuttleCardTime = itemView!!.findViewById<TextView>(R.id.shuttle_timetable_time)


        fun bind(item: ShuttleItem){
            shuttleCardHeading.text = getHeadingString(item.type)
            shuttleCardTime.text = item.time
            if(now.isAfter(LocalTime.parse(item.time, formatter))){
                shuttleCardHeading.setTextColor(Color.GRAY)
                shuttleCardTime.setTextColor(Color.GRAY)
            } else {
                shuttleCardHeading.setTextColor(mContext.getColor(R.color.primaryTextColor))
                shuttleCardTime.setTextColor(mContext.getColor(R.color.primaryTextColor))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_shuttle_timetable, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getHeadingString(type: String): String{
        return if(type == "C"){
            mContext.getString(R.string.cycle)
        } else{
            mContext.getString(R.string.direct)
        }
    }
}