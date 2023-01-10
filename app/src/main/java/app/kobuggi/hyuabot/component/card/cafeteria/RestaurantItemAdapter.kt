package app.kobuggi.hyuabot.component.card.cafeteria

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemRestaurantBinding
import app.kobuggi.hyuabot.databinding.ItemSubwayHeadingBinding
import app.kobuggi.hyuabot.model.cafeteria.RestaurantItemResponse
import java.time.LocalTime

class RestaurantItemAdapter (private val context: Context, private val restaurantList: List<RestaurantItemResponse>) : RecyclerView.Adapter<RestaurantItemAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemRestaurantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(restaurantItem: RestaurantItemResponse) {
            val decoration = DividerItemDecoration(context, VERTICAL)
            binding.restaurantName.text = restaurantItem.name
            binding.restaurantMenu.adapter = MenuItemAdapter(restaurantItem.menu[0].menu)
            binding.restaurantMenu.layoutManager = LinearLayoutManager(context)
            if (restaurantItem.menu[0].menu.size > 1){
                binding.restaurantMenu.addItemDecoration(decoration)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return ViewHolder(ItemRestaurantBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(restaurantList[position])
    }

    override fun getItemCount(): Int = restaurantList.size
}