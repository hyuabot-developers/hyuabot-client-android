package app.kobuggi.hyuabot.ui.cafeteria

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemMenuBinding
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator
import java.util.Locale

class MenuListAdapter(
    private val context: Context,
    private var menuList: List<CafeteriaPageQuery.Menu> = emptyList(),
) : RecyclerView.Adapter<MenuListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cafeteriaItem: CafeteriaPageQuery.Menu) {
            binding.apply {
                DynamicTextTranslator.bind(
                    menuTextView,
                    localizedFood(cafeteriaItem.food),
                    context.getString(R.string.cafeteria_menu_translating),
                )
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
    fun updateList(newList: List<CafeteriaPageQuery.Menu>) {
        menuList = newList
        notifyDataSetChanged()
    }

    private fun localizedFood(food: String): String {
        val cleaned = food.replace("\"", "").trim()
        val appLanguage = context.resources.configuration.locales[0]?.language ?: Locale.KOREAN.language
        if (!appLanguage.startsWith(Locale.KOREAN.language)) return cleaned

        val koreanTokens = cleaned
            .split(Regex("\\s+"))
            .filter { token -> token.any(::isHangul) }
            .joinToString(" ")

        return koreanTokens.ifBlank { cleaned }
    }

    private fun isHangul(char: Char): Boolean {
        return char in '\u1100'..'\u11FF' ||
            char in '\u3130'..'\u318F' ||
            char in '\uAC00'..'\uD7A3'
    }
}
