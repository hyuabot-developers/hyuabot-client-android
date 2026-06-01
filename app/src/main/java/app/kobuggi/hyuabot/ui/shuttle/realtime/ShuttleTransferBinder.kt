package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleTransferQuery
import app.kobuggi.hyuabot.util.buildTransitRows

object ShuttleTransferBinder {
    fun bind(
        section: View,
        container: LinearLayout,
        stopName: String,
        data: ShuttleTransferQuery.Data?,
    ) {
        val rows = data?.let { buildTransitRows(container.context, stopName, it) } ?: emptyList()
        container.removeAllViews()
        if (rows.isEmpty()) {
            section.visibility = View.GONE
            return
        }
        val inflater = LayoutInflater.from(container.context)
        rows.forEach { row ->
            val view = inflater.inflate(R.layout.item_shuttle_transfer, container, false)
            val name = view.findViewById<TextView>(R.id.transfer_name)
            name.text = row.name
            name.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(container.context, row.colorRes))
            view.findViewById<TextView>(R.id.transfer_detail).text = row.detail
            container.addView(view)
        }
        section.visibility = View.VISIBLE
    }
}
