package app.kobuggi.hyuabot.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.activity.ShuttleActivity
import app.kobuggi.hyuabot.model.BusCardItem
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BusCardListAdapter(private val list : ArrayList<BusCardItem>, private val mContext: Context) : RecyclerView.Adapter<BusCardListAdapter.ItemViewHolder>(){
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    private val day = LocalDate.now()
    private val now = LocalTime.now()

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val cardLineID = itemView!!.findViewById<TextView>(R.id.bus_card_line_id)
        private val cardStopName = itemView!!.findViewById<TextView>(R.id.bus_card_bus_stop)
        private val cardHeading: TextView = itemView!!.findViewById(R.id.bus_card_heading)
        private val cardThisBus = itemView!!.findViewById<TextView>(R.id.bus_card_this)
        private val cardNextBus = itemView!!.findViewById<TextView>(R.id.bus_card_next)


        fun bind(item: BusCardItem){
            cardLineID.setTextColor(Color.parseColor(item.lineColor))
            cardLineID.text = item.lineName
            cardStopName.text = item.busStop
            cardHeading.text = mContext.getString(R.string.bus_heading_to, item.heading)

            val timetableData = when(day.dayOfWeek){
                DayOfWeek.SATURDAY -> item.busData.timetable.sat
                DayOfWeek.SUNDAY -> item.busData.timetable.sun
                else -> item.busData.timetable.weekdays
            }.filter { Duration.between(now, LocalTime.parse(it.time, formatter)).toMinutes() > 0 }

            when(item.busData.realtime.size){
                0 -> {
                    when(timetableData.size){
                        0 -> {
                            cardThisBus.text = mContext.getString(R.string.out_of_service)
                            cardNextBus.text = ""
                        }
                        1 -> {
                            cardThisBus.text = mContext.getString(R.string.bus_remained_time_waiting, Duration.between(now, LocalTime.parse(timetableData[0].time, formatter)).toMinutes() + item.minutesFromTerminalStop)
                            cardNextBus.text = ""
                        }
                        else -> {
                            cardThisBus.text = mContext.getString(R.string.bus_remained_time_waiting, Duration.between(now, LocalTime.parse(timetableData[0].time, formatter)).toMinutes() + item.minutesFromTerminalStop)
                            cardNextBus.text = mContext.getString(R.string.bus_remained_time_waiting, Duration.between(now, LocalTime.parse(timetableData[1].time, formatter)).toMinutes() + item.minutesFromTerminalStop)
                        }
                    }
                }
                1 -> {
                    cardThisBus.text = mContext.getString(R.string.bus_remained_time, item.busData.realtime[0].time, item.busData.realtime[0].location)
                    if (timetableData.isEmpty()){
                        cardNextBus.text = mContext.getString(R.string.last_bus)
                    } else {
                        cardNextBus.text = mContext.getString(R.string.bus_remained_time_waiting, Duration.between(now, LocalTime.parse(timetableData[0].time, formatter)).toMinutes() + item.minutesFromTerminalStop)
                    }
                }
                else -> {
                    cardThisBus.text = mContext.getString(R.string.bus_remained_time, item.busData.realtime[0].time, item.busData.realtime[0].location)
                    cardNextBus.text = mContext.getString(R.string.bus_remained_time, item.busData.realtime[1].time, item.busData.realtime[1].location)
                }
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_bus, parent, false)
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