package app.kobuggi.hyuabot.ui.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemMainMenuBinding

class MenuListAdapter(
    private val context: Context,
    private var menuList: List<MenuItem>,
    private val onClickListener: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemMainMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: MenuItem) {
            binding.apply {
                menuItemView.setOnClickListener { onClickListener(menuItem) }
                menuIconView.setImageResource(menuItem.iconResource)
                menuTextView.text = context.getString(menuItem.titleResource)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_main_menu, parent, false)
        return ViewHolder(ItemMainMenuBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount() = menuList.size
}
