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
import app.kobuggi.hyuabot.model.SubwayCardItem
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class SubwayCardListAdapter(private val list : ArrayList<SubwayCardItem>, private val mContext: Context) : RecyclerView.Adapter<SubwayCardListAdapter.ItemViewHolder>(){

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val cardTitle = itemView!!.findViewById<TextView>(R.id.subway_card_title)
        private val cardSubTitle = itemView!!.findViewById<TextView>(R.id.subway_card_subtitle)
        private val currentStationIcon = itemView!!.findViewById<ImageView>(R.id.subway_current_circle)
        private val cardThisSubway = itemView!!.findViewById<TextView>(R.id.subway_card_this)
        private val cardNextSubway = itemView!!.findViewById<TextView>(R.id.subway_card_next)
        private val updatedTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val subwayFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        private val subwayFormatterNoSecond = DateTimeFormatter.ofPattern("HH:mm")


        fun bind(item: SubwayCardItem){
            val now = LocalDateTime.now()

            cardTitle.text = mContext.getString(R.string.subway_card_title, item.lineName, mContext.getString(R.string.subway_current_station))
            cardSubTitle.text = mContext.getString(R.string.bus_heading_to, item.heading)
            currentStationIcon.setImageResource(item.lineIconResID)

            var length = 0

            if(item.realtime != null){
                for(realtimeItem in item.realtime){
                    val updatedTime = LocalDateTime.parse(realtimeItem.updatedTime.replace("T", " ").replace("+09:00", ""), updatedTimeFormatter)
                    if(realtimeItem.pos != "null" && (realtimeItem.time - Duration.between(updatedTime, now).toMinutes()) > 0){
                        if(length == 0){
                            cardThisSubway.text = mContext.resources.getString(R.string.subway_departure_arrival, (realtimeItem.time - Duration.between(updatedTime, now).toMinutes()).toInt(), realtimeItem.terminalStn)
                            length++
                        } else if (length == 1){
                            cardNextSubway.text = mContext.resources.getString(R.string.subway_departure_arrival, (realtimeItem.time - Duration.between(updatedTime, now).toMinutes()).toInt(), realtimeItem.terminalStn)
                            length++
                        } else{
                            break
                        }
                    }
                }
            }

            if(item.timetable != null && length < 2){
                var findLaterTimetable = 0.0F
                if(length == 1){
                    findLaterTimetable = item.realtime?.get(0)!!.time - Duration.between(LocalDateTime.parse(
                        item.realtime[0].updatedTime.replace("T", " ").replace("+09:00", ""), updatedTimeFormatter), now).toMinutes()
                }
                for(timetableItem in item.timetable){
                    Log.d("timetable", timetableItem.time.toString())
                    val duration : Long = if (timetableItem.time.split(":").size == 3){
                        Duration.between(now.toLocalTime(), LocalTime.parse(timetableItem.time, subwayFormatter)).toMinutes()
                    } else{
                        Duration.between(now.toLocalTime(), LocalTime.parse(timetableItem.time, subwayFormatterNoSecond)).toMinutes()
                    }
                    if(duration > findLaterTimetable){
                        if(length == 0){
                            cardThisSubway.text = mContext.resources.getString(R.string.subway_departure_arrival, duration, timetableItem.terminalStn)
                            length++
                        } else if(length == 1){
                            cardNextSubway.text = mContext.resources.getString(R.string.subway_departure_arrival, duration, timetableItem.terminalStn)
                            length++
                        } else{
                            break
                        }
                    }
                }
            }

            if (length == 0){
                cardThisSubway.text = mContext.getString(R.string.out_of_service)
                cardNextSubway.visibility = View.INVISIBLE
            } else if(length == 1){
                cardNextSubway.text = mContext.getString(R.string.out_of_service)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_subway, parent, false)
        view.setOnClickListener {
            val shuttleActivity = Intent(mContext, ShuttleActivity::class.java)
            mContext.startActivity(shuttleActivity)
        }
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}