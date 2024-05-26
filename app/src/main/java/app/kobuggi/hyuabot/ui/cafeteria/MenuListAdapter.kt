package app.kobuggi.hyuabot.ui.cafeteria

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemMenuBinding

class MenuListAdapter(
    private val context: Context,
    private var menuList: List<CafeteriaPageQuery.Menu1>,
) : RecyclerView.Adapter<MenuListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cafeteriaItem: CafeteriaPageQuery.Menu1) {
            binding.apply {
                menuTextView.text = cafeteriaItem.menu
                if (cafeteriaItem.price.endsWith("원")) {
                    menuPriceView.text = context.getString(R.string.cafeteria_price_format, cafeteriaItem.price.replace("원", ""))
                } else {
                    menuPriceView.text = context.getString(R.string.cafeteria_price_format, cafeteriaItem.price)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false)
        return ViewHolder(ItemMenuBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount() = menuList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<CafeteriaPageQuery.Menu1>) {
        menuList = newList
        notifyDataSetChanged()
    }
}
