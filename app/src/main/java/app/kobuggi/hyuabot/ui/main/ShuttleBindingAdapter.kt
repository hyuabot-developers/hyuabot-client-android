package app.kobuggi.hyuabot.ui.main

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.model.ShuttleDataItem

@BindingAdapter(value = ["repositories", "viewModel"])
fun setShuttleArrivalCard(view: RecyclerView, items: List<ShuttleDataItem>, vm: ShuttleViewModel) {
    view.adapter?.run {
        if (this is ShuttleCardListAdapter) {
            this. = items
            this.notifyDataSetChanged()
        }
    } ?: run {
        ShuttleCardListAdapter(items, vm, ).apply { view.adapter = this }
    }
}