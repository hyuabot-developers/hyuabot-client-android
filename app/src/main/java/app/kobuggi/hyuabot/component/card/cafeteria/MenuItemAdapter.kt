package app.kobuggi.hyuabot.component.card.cafeteria

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemMenuBinding
import app.kobuggi.hyuabot.model.cafeteria.RestaurantMenuItem

class MenuItemAdapter(private val menuList: List<RestaurantMenuItem>) : RecyclerView.Adapter<MenuItemAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: RestaurantMenuItem) {
            val resources = GlobalApplication.getAppResources()
            binding.menuFood.text = menuItem.food
            binding.menuPrice.text = resources.getString(R.string.food_price, menuItem.price)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return ViewHolder(ItemMenuBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int = menuList.size
}