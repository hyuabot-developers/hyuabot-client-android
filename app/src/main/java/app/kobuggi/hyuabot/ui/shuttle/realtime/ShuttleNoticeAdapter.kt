package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemNoticeBinding
import app.kobuggi.hyuabot.util.NavControllerExtension.safeNavigate

class ShuttleNoticeAdapter(private var items: List<ShuttleRealtimePageQuery.Notice1>): RecyclerView.Adapter<ShuttleNoticeAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemNoticeBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShuttleRealtimePageQuery.Notice1) {
            binding.tvNoticeTitle.apply {
                text = item.title
                if (item.url.isEmpty()) {
                    setOnClickListener(null)
                } else {
                    setOnClickListener {
                        ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToNoticeWebViewFragment(item.url).let { direction ->
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

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<ShuttleRealtimePageQuery.Notice1>) {
        items = newList
        notifyDataSetChanged()
    }
}
