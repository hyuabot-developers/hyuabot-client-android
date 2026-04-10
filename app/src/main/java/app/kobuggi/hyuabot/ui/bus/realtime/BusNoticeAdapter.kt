package app.kobuggi.hyuabot.ui.bus.realtime

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.BusRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemNoticeBinding

class BusNoticeAdapter(private var items: List<BusRealtimePageQuery.Notice1>): RecyclerView.Adapter<BusNoticeAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemNoticeBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BusRealtimePageQuery.Notice1) {
            binding.tvNoticeTitle.apply {
                text = item.title
                if (item.url.isEmpty()) {
                    setOnClickListener(null)
                } else {
                    setOnClickListener {
                        BusRealtimeFragmentDirections.actionBusRealtimeFragmentToNoticeWebViewFragment(item.url).let { direction ->
                            it.findNavController().navigate(direction)
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

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<BusRealtimePageQuery.Notice1>) {
        items = newList
        notifyDataSetChanged()
    }
}
