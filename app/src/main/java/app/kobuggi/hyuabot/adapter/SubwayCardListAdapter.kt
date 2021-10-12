package app.kobuggi.hyuabot.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SubwayCardListAdapter(private val list : ArrayList<SubwayCardItem>, private val mContext: Context) : RecyclerView.Adapter<SubwayCardListAdapter.ItemViewHolder>(){
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val cardTitle = itemView!!.findViewById<TextView>(R.id.subway_card_title)
        private val cardSubTitle = itemView!!.findViewById<TextView>(R.id.subway_card_subtitle)
        private val currentStationIcon = itemView!!.findViewById<ImageView>(R.id.subway_current_circle)
        private val cardThisSubway = itemView!!.findViewById<TextView>(R.id.subway_card_this)
        private val cardNextSubway = itemView!!.findViewById<TextView>(R.id.subway_card_next)


        fun bind(item: SubwayCardItem){
            val now = LocalTime.now()

            cardTitle.text = mContext.getString(R.string.subway_card_title, item.lineName, mContext.getString(R.string.subway_current_station))
            cardSubTitle.text = mContext.getString(R.string.bus_heading_to, item.heading)
            currentStationIcon.setImageResource(item.lineIconResID)
            when{
                item.realtime.size >= 2 -> {
                    cardThisSubway.text = mContext.getString(R.string.this_bus, item.realtime[0].time.toInt(), item.realtime[0].terminalStn)
                    cardNextSubway.text = mContext.getString(R.string.this_bus, item.realtime[1].time.toInt(), item.realtime[1].terminalStn)
                }
                item.realtime.size == 1 -> {
                    cardThisSubway.text = mContext.getString(R.string.this_bus, item.realtime[0].time.toInt(), item.realtime[0].terminalStn)
                    if(item.timetable.size == 1){
                        cardNextSubway.text = mContext.getString(R.string.this_bus, Duration.between(now, LocalTime.parse(item.timetable[0].time, formatter)).toMinutes(), item.timetable[0].terminalStn)
                    } else {
                        cardNextSubway.text = mContext.getString(R.string.out_of_service)
                    }
                }
                else -> {
                    when {
                        item.timetable.size >= 2 -> {
                            cardThisSubway.text = mContext.getString(R.string.this_bus, Duration.between(now, LocalTime.parse(item.timetable[0].time, formatter)).toMinutes(), item.timetable[0].terminalStn)
                            cardNextSubway.text = mContext.getString(R.string.this_bus, Duration.between(now, LocalTime.parse(item.timetable[1].time, formatter)).toMinutes(), item.timetable[1].terminalStn)
                        }
                        item.timetable.size == 1 -> {
                            cardThisSubway.text = mContext.getString(R.string.this_bus, Duration.between(now, LocalTime.parse(item.timetable[0].time, formatter)).toMinutes(), item.timetable[0].terminalStn)
                            cardNextSubway.text = ""
                        }
                        else -> {
                            cardNextSubway.text = mContext.getString(R.string.out_of_service)
                        }
                    }
                }
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