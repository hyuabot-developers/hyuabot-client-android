package app.kobuggi.hyuabot.ui.cafeteria

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemCafeteriaBinding

class CafeteriaListAdapter(
    private val context: Context,
    private val type: String,
    private var cafeteriaList: List<CafeteriaPageQuery.Menu>,
) : RecyclerView.Adapter<CafeteriaListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemCafeteriaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cafeteriaItem: CafeteriaPageQuery.Menu) {
            val menuAdapter = MenuListAdapter(context, listOf())
            if (type == "breakfast") {
                menuAdapter.updateList(cafeteriaItem.menu.filter { it.type.contains("조식") }.distinctBy { it.menu } )
            } else if (type == "lunch") {
                menuAdapter.updateList(cafeteriaItem.menu.filter { it.type.contains("중식") }.distinctBy { it.menu } )
            } else if (type == "dinner") {
                menuAdapter.updateList(cafeteriaItem.menu.filter { it.type.contains("석식") }.distinctBy { it.menu } )
            }
            val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            binding.apply {
                headerCafeteria.text = getCafeteriaString(cafeteriaItem.id)
                menuList.apply {
                    adapter = menuAdapter
                    addItemDecoration(decoration)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cafeteria, parent, false)
        return ViewHolder(ItemCafeteriaBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cafeteriaList[position])
    }

    override fun getItemCount() = cafeteriaList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<CafeteriaPageQuery.Menu>) {
        cafeteriaList = newList
        notifyDataSetChanged()
    }

    private fun getCafeteriaString(cafeteriaID: Int): String {
        return when (cafeteriaID) {
            1 -> context.getString(R.string.cafeteria_1)
            2 -> context.getString(R.string.cafeteria_2)
            4 -> context.getString(R.string.cafeteria_4)
            6 -> context.getString(R.string.cafeteria_6)
            7 -> context.getString(R.string.cafeteria_7)
            8 -> context.getString(R.string.cafeteria_8)
            11 -> context.getString(R.string.cafeteria_11)
            12 -> context.getString(R.string.cafeteria_12)
            13 -> context.getString(R.string.cafeteria_13)
            14 -> context.getString(R.string.cafeteria_14)
            15 -> context.getString(R.string.cafeteria_15)
            else -> context.getString(R.string.cafeteria_1)
        }
    }
}
