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
import app.kobuggi.hyuabot.util.DividerItemWithoutLastDecoration
import java.time.LocalTime

class CafeteriaListAdapter(
    private val context: Context,
    private val type: String,
    private var cafeteriaList: List<CafeteriaPageQuery.Cafeterium>,
) : RecyclerView.Adapter<CafeteriaListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemCafeteriaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cafeteriaItem: CafeteriaPageQuery.Cafeterium) {
            val menuAdapter = MenuListAdapter(context, listOf())
            val runningTime = when (type) {
                "breakfast" -> {
                    cafeteriaItem.runningTime.breakfast ?: "-"
                }
                "lunch" -> {
                    cafeteriaItem.runningTime.lunch ?: "-"
                }
                "dinner" -> {
                    cafeteriaItem.runningTime.dinner ?: "-"
                }
                else -> "-"
            }
            val menus = cafeteriaItem.menus.filter { it.type.contains(mealTypeQuery(type)) }
                .distinctBy { it.food }
            menuAdapter.updateList(menus)
            val decoration = DividerItemWithoutLastDecoration(context, DividerItemDecoration.VERTICAL)
            binding.apply {
                headerCafeteria.text = getCafeteriaString(cafeteriaItem.seq)
                subheaderCafeteria.text = context.getString(
                    R.string.cafeteria_running_time_status_format,
                    runningTime,
                    statusText(runningTime, menus.isNotEmpty())
                )
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
    fun updateList(newList: List<CafeteriaPageQuery.Cafeterium>) {
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

    private fun statusText(runningTime: String, hasMenu: Boolean): String {
        if (!hasMenu) return context.getString(R.string.cafeteria_status_no_menu)
        val times = Regex("""(\d{1,2}):(\d{2})""").findAll(runningTime)
            .mapNotNull {
                val hour = it.groupValues[1].toIntOrNull() ?: return@mapNotNull null
                val minute = it.groupValues[2].toIntOrNull() ?: return@mapNotNull null
                runCatching { LocalTime.of(hour, minute) }.getOrNull()
            }
            .take(2)
            .toList()
        if (times.size < 2) return context.getString(R.string.cafeteria_status_has_menu)
        val now = LocalTime.now()
        return when {
            now.isBefore(times[0]) -> context.getString(R.string.cafeteria_status_soon)
            now.isAfter(times[1]) -> context.getString(R.string.cafeteria_status_closed)
            else -> context.getString(R.string.cafeteria_status_open)
        }
    }
}
