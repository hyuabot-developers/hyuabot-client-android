package app.kobuggi.hyuabot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.model.MenuItem
import app.kobuggi.hyuabot.model.Restaurant
import app.kobuggi.hyuabot.model.RestaurantList
import java.time.LocalDateTime

class RestaurantCardListAdapter(list: RestaurantList) : RecyclerView.Adapter<RestaurantCardListAdapter.ItemViewHolder>(){
    private val mList = list
    private val now = LocalDateTime.now()

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val foodCardTitle = itemView!!.findViewById<TextView>(R.id.food_card_title)
        private val foodCardMenu = itemView!!.findViewById<TextView>(R.id.food_card_menu)
        private val foodCardPrice = itemView!!.findViewById<TextView>(R.id.food_card_price)
        lateinit var currentMenuKey : String
        private var currentMenu : List<MenuItem>? = null

        @SuppressLint("SetTextI18n")
        fun bind(item: Restaurant){
            foodCardTitle.text = item.Name
            when(now.hour){
                in 0 .. 10 -> currentMenuKey = "조식"
                in 11 .. 15 -> currentMenuKey = "중식"
                else -> currentMenuKey = "석식"
            }
            currentMenu = item.MenuList[currentMenuKey]
            if(currentMenu != null){
                foodCardMenu.text = currentMenu!![0].Menu
                foodCardPrice.text = "${currentMenu!![0].Price} 원"
            } else{
                foodCardMenu.text = "-"
                foodCardPrice.text = "0 원"
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.size
    }


}