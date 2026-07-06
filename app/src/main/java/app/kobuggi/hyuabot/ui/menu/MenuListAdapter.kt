package app.kobuggi.hyuabot.ui.menu
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemMainMenuBinding

class MenuListAdapter(
    private val context: Context,
    private var menuList: List<MenuItem>,
    private val onClickListener: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuListAdapter.ViewHolder>() {
    private val sections = menuList.toSections()

    inner class ViewHolder(private val binding: ItemMainMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        private fun bindSection(section: MenuSection) {
            binding.apply {
                menuSectionTextView.text = context.getString(section.titleResource)
                menuItemsContainer.removeAllViews()
                section.items.forEachIndexed { index, menuItem ->
                    if (index > 0) {
                        LayoutInflater.from(context).inflate(R.layout.item_main_menu_divider, menuItemsContainer, false).also {
                            menuItemsContainer.addView(it)
                        }
                    }
                    val row = LayoutInflater.from(context).inflate(R.layout.item_main_menu_row, menuItemsContainer, false)
                    row.setOnClickListener {
                        AnalyticsManager.logSelect(AnalyticsItem.MENU_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM)
                        onClickListener(menuItem)
                    }
                    row.findViewById<ImageView>(R.id.menu_icon_view).setImageResource(menuItem.iconResource)
                    row.findViewById<TextView>(R.id.menu_text_view).text = context.getString(menuItem.titleResource)
                    row.findViewById<View>(R.id.menu_arrow_view).isVisible = true
                    menuItemsContainer.addView(row)
                }
            }
        }

        fun bind(position: Int) {
            bindSection(sections[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_main_menu, parent, false)
        return ViewHolder(ItemMainMenuBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = sections.size

    private fun List<MenuItem>.toSections(): List<MenuSection> {
        val result = mutableListOf<MenuSection>()
        var currentTitle: Int? = null
        var currentItems = mutableListOf<MenuItem>()
        forEach { item ->
            if (item.sectionTitleResource != null) {
                if (currentTitle != null && currentItems.isNotEmpty()) {
                    result += MenuSection(currentTitle, currentItems)
                }
                currentTitle = item.sectionTitleResource
                currentItems = mutableListOf()
            }
            currentItems += item
        }
        if (currentTitle != null && currentItems.isNotEmpty()) {
            result += MenuSection(currentTitle, currentItems)
        }
        return result
    }

    private data class MenuSection(
        val titleResource: Int,
        val items: List<MenuItem>
    )
}
