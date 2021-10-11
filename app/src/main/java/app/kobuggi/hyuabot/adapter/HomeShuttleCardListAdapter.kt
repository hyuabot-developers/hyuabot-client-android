package app.kobuggi.hyuabot.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.activity.ShuttleActivity
import app.kobuggi.hyuabot.model.ShuttleDataItem
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HomeShuttleCardListAdapter(private val list: List<ShuttleDataItem>, private val mContext: Context) : RecyclerView.Adapter<HomeShuttleCardListAdapter.ItemViewHolder>(){
    private val now = LocalTime.now()
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")


    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val shuttleCardTitle = itemView!!.findViewById<TextView>(R.id.shuttle_card_title)
        private val shuttleCardRemainedTime = itemView!!.findViewById<TextView>(R.id.shuttle_card_time)
        private val shuttleCardThisBus = itemView!!.findViewById<TextView>(R.id.shuttle_card_this_bus)
        private val shuttleCardNextBus = itemView!!.findViewById<TextView>(R.id.shuttle_card_next_bus)

        fun bind(item: ShuttleDataItem){
            shuttleCardTitle.text = mContext.resources.getString(item.cardTitle)
            when(item.arrivalList.size){
                0 -> {
                    shuttleCardRemainedTime.text = mContext.resources.getString(R.string.out_of_service)
                    shuttleCardThisBus.text = mContext.resources.getString(R.string.no_this_bus)
                    shuttleCardNextBus.text = mContext.resources.getString(R.string.no_next_bus)
                }
                1 -> {
                    shuttleCardRemainedTime.text = mContext.resources.getString(R.string.shuttle_remained_time, Duration.between(now, LocalTime.parse(item.arrivalList[0].time, formatter)).toMinutes().toInt())
                    shuttleCardThisBus.text = mContext.resources.getString(R.string.this_bus, item.arrivalList[0].time, getHeadingString(item.arrivalList[0].type))
                    shuttleCardNextBus.text = mContext.resources.getString(R.string.no_next_bus)
                    (shuttleCardRemainedTime.text as Spannable).setSpan(RelativeSizeSpan(0.75f), shuttleCardRemainedTime.text.length - 6, shuttleCardRemainedTime.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                else ->{
                    shuttleCardRemainedTime.text = mContext.resources.getString(R.string.shuttle_remained_time, Duration.between(now, LocalTime.parse(item.arrivalList[0].time, formatter)).toMinutes().toInt())
                    shuttleCardThisBus.text = mContext.resources.getString(R.string.this_bus, item.arrivalList[0].time, getHeadingString(item.arrivalList[0].type))
                    shuttleCardNextBus.text = mContext.resources.getString(R.string.this_bus, item.arrivalList[1].time, getHeadingString(item.arrivalList[1].type))
                    (shuttleCardRemainedTime.text as Spannable).setSpan(RelativeSizeSpan(0.75f), shuttleCardRemainedTime.text.length - 6, shuttleCardRemainedTime.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_shuttle_home, parent, false)
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