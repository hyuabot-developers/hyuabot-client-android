package app.kobuggi.hyuabot.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.activity.ShuttleActivity
import app.kobuggi.hyuabot.model.ShuttleCardItem
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class ShuttleCardListAdapter(private val list: List<ShuttleCardItem>, private val mContext: Context, private val subwayDataType : Int) : RecyclerView.Adapter<ShuttleCardListAdapter.ItemViewHolder>(){
    private val now = LocalTime.now()
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    private val subwayFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val updatedTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val shuttleCardTitle = itemView!!.findViewById<TextView>(R.id.shuttle_card_bus_stop)
        private val shuttleCardSubTitle = itemView!!.findViewById<TextView>(R.id.shuttle_card_heading)
        private val shuttleCardThisBus = itemView!!.findViewById<TextView>(R.id.shuttle_card_this)
        private val shuttleCardNextBus = itemView!!.findViewById<TextView>(R.id.shuttle_card_next)
        private val shuttleCardMetroArrow = itemView!!.findViewById<ImageView>(R.id.shuttle_card_metro_arrow)
        private val shuttleCardThisSubway = itemView!!.findViewById<TextView>(R.id.shuttle_card_this_subway)
        private val shuttleCardNextSubway = itemView!!.findViewById<TextView>(R.id.shuttle_card_next_subway)

        fun bind(item: ShuttleCardItem){
            shuttleCardTitle.text = mContext.resources.getString(item.shuttleStopID)
            shuttleCardSubTitle.text = mContext.resources.getString(R.string.shuttle_heading, mContext.resources.getString(item.headingID))

            when(item.arrivalList.size){
                0 -> {
                    shuttleCardThisBus.text = mContext.resources.getString(R.string.no_this_bus)
                    shuttleCardNextBus.text = mContext.resources.getString(R.string.no_next_bus)
                }
                1 -> {
                    shuttleCardThisBus.text = mContext.resources.getString(R.string.this_bus, Duration.between(now, LocalTime.parse(item.arrivalList[0].time, formatter)).toMinutes().toInt(), getHeadingString(item.arrivalList[0].type))
                    shuttleCardNextBus.text = mContext.resources.getString(R.string.no_next_bus)
                }
                else ->{
                    shuttleCardThisBus.text = mContext.resources.getString(R.string.this_bus, Duration.between(now, LocalTime.parse(item.arrivalList[0].time, formatter)).toMinutes().toInt(), getHeadingString(item.arrivalList[0].type))
                    shuttleCardNextBus.text = mContext.resources.getString(R.string.next_bus, Duration.between(now, LocalTime.parse(item.arrivalList[1].time, formatter)).toMinutes().toInt(), getHeadingString(item.arrivalList[1].type))
                }
            }

            if(item.headingID == R.string.station){
                shuttleCardMetroArrow.visibility = View.VISIBLE
                shuttleCardThisSubway.visibility = View.VISIBLE
                shuttleCardNextSubway.visibility = View.VISIBLE
                var length = 0


                if(item.subwayItemsRealtime != null){
                    for(realtimeItem in item.subwayItemsRealtime){
                        val updatedTime = LocalDateTime.parse(realtimeItem.updatedTime.replace("T", " ").replace("+09:00", ""), updatedTimeFormatter)
                        if(realtimeItem.pos != "null" && realtimeItem.time - Duration.between(updatedTime, now).toMinutes() > Duration.between(now, LocalTime.parse(item.arrivalList[0].time, formatter)).toMinutes().toInt() && length == 0){
                            shuttleCardThisSubway.text = mContext.resources.getString(R.string.subway_departure_arrival, (realtimeItem.time - Duration.between(now, updatedTime).toMinutes()).toInt(), realtimeItem.terminalStn)
                            length++
                        } else if(realtimeItem.pos != "null" && realtimeItem.time - Duration.between(updatedTime, now).toMinutes() > Duration.between(now, LocalTime.parse(item.arrivalList[1].time, formatter)).toMinutes().toInt() && length == 1){
                            shuttleCardNextSubway.text = mContext.resources.getString(R.string.subway_departure_arrival, (realtimeItem.time - Duration.between(now, updatedTime).toMinutes()).toInt(), realtimeItem.terminalStn)
                            length++
                        } else if(length >= 2){
                            break
                        }
                    }
                }
                if(item.subwayItemsTimetable != null && length < 2){
                    var findLaterTimetable = 0.0F
                    if(length == 1){
                        findLaterTimetable = item.subwayItemsRealtime?.get(0)!!.time - Duration.between(now, LocalDateTime.parse(
                            item.subwayItemsRealtime[0].updatedTime.replace("T", " ").replace("+09:00", ""), updatedTimeFormatter)).toMinutes()
                    }
                    for(timetableItem in item.subwayItemsTimetable){

                        if(Duration.between(now, LocalTime.parse(timetableItem.time, subwayFormatter)).toMinutes() > max(Duration.between(now, LocalTime.parse(item.arrivalList[0].time, formatter)).toMinutes().toInt(), findLaterTimetable.toInt()) && length == 0){
                            shuttleCardThisSubway.text = mContext.resources.getString(R.string.subway_departure_arrival, Duration.between(now, LocalTime.parse(timetableItem.time, subwayFormatter)).toMinutes(), timetableItem.terminalStn)
                            length++
                        } else if(Duration.between(now, LocalTime.parse(timetableItem.time, subwayFormatter)).toMinutes() >  max(Duration.between(now, LocalTime.parse(item.arrivalList[1].time, formatter)).toMinutes().toInt(), findLaterTimetable.toInt()) && length == 1){
                            shuttleCardNextSubway.text = mContext.resources.getString(R.string.subway_departure_arrival, Duration.between(now, LocalTime.parse(timetableItem.time, subwayFormatter)).toMinutes(), timetableItem.terminalStn)
                            length++
                        } else if(length >= 2){
                            break
                        }
                    }
                } else if (length == 0){
                    shuttleCardMetroArrow.visibility = View.INVISIBLE
                    shuttleCardThisSubway.visibility = View.INVISIBLE
                    shuttleCardNextSubway.visibility = View.INVISIBLE
                } else if(length == 1){
                    shuttleCardThisSubway.visibility = View.INVISIBLE
                }

                if (subwayDataType <= 1){
                    shuttleCardMetroArrow.setImageResource(R.drawable.ic_arrow_skyblue)
                } else {
                    shuttleCardMetroArrow.setImageResource(R.drawable.ic_arrow_yellow)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_shuttle, parent, false)
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

    private fun getHeadingString(heading: String) : String{
        return  if (heading == "C") "순환" else "직행"
    }
}