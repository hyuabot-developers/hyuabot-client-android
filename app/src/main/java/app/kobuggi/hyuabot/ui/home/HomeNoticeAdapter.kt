package app.kobuggi.hyuabot.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.HomePageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.ItemNoticeBinding
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.NavControllerExtension.safeNavigate

class HomeNoticeAdapter(
    private var items: List<HomePageQuery.Notice1>,
) : RecyclerView.Adapter<HomeNoticeAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomePageQuery.Notice1) {
            binding.tvNoticeTitle.apply {
                setTextColor(ContextCompat.getColor(context, R.color.home_notice_text))
                DynamicTextTranslator.bind(this, item.title)
                if (item.url.isEmpty()) {
                    setOnClickListener(null)
                } else {
                    setOnClickListener {
                        AnalyticsManager.logSelect(AnalyticsItem.NOTICE_OPEN)
                        HomeFragmentDirections.actionHomeFragmentToNoticeWebViewFragment(item.url).let { direction ->
                            it.findNavController().safeNavigate(direction)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
        return ViewHolder(ItemNoticeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<HomePageQuery.Notice1>) {
        items = newList
        notifyDataSetChanged()
    }
}
