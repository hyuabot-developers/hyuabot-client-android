package app.kobuggi.hyuabot.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.model.MenuItem
import app.kobuggi.hyuabot.model.MenuListItem
import app.kobuggi.hyuabot.model.Restaurant
import app.kobuggi.hyuabot.model.RestaurantList
import java.time.LocalDateTime

class RestaurantCardMenuAdapter(private val mList: List<MenuListItem>) : RecyclerView.Adapter<RestaurantCardMenuAdapter.ItemViewHolder>(){
    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val foodCardMenu = itemView!!.findViewById<TextView>(R.id.food_card_menu)
        private val foodCardPrice = itemView!!.findViewById<TextView>(R.id.food_card_price)

        @SuppressLint("SetTextI18n")
        fun bind(item: MenuListItem){
            foodCardMenu.text = item.menuItem.Menu.replace("[", "[${item.key} - ")
            foodCardPrice.text = "${item.menuItem.Price}원"

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_food_menu, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
        if (position == mList.size - 1){
            holder.itemView.findViewById<View>(R.id.food_card_divider).visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }


}