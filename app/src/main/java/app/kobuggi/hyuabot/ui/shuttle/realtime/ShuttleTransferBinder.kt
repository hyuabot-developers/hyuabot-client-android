package app.kobuggi.hyuabot.ui.shuttle.realtime
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemShuttleConnectionRowBinding
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.util.TransitRow
import app.kobuggi.hyuabot.util.buildTransitRows

fun Fragment.bindShuttleHelpButtons(vararg buttons: View) {
    buttons.forEach { button ->
        button.setOnClickListener {
            AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_OPEN_HELP)
            ShuttleRealtimeFragmentDirections.actionShuttleRealtimeFragmentToShuttleHelpDialogFragment().also {
                findNavController().safeNavigate(it)
            }
        }
    }
}

object ShuttleTransferBinder {
    fun bind(
        section: View,
        container: LinearLayout,
        stopName: String,
        data: ShuttleRealtimePageQuery.Data?,
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
            val timeline = view.findViewById<TransitTimelineView>(R.id.transfer_timeline)
            timeline.bind(row)
            container.addView(view)
        }
        section.visibility = View.VISIBLE
    }

    fun bindCompact(container: LinearLayout, rows: List<TransitRow>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(container.context)
        val darkMode = container.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        rows.forEach { row ->
            val binding = ItemShuttleConnectionRowBinding.inflate(inflater, container, false)
            val tint = ContextCompat.getColor(container.context, row.colorRes)
            binding.transferAccent.setBackgroundColor(tint)
            binding.transferTitle.text = row.compactTitle
            binding.transferTitle.setTextColor(tint)
            binding.transferTrailing.text = row.compactTrailing
            binding.transferConnector.visibility = if (row.connectorTitle == null) View.GONE else View.VISIBLE
            if (row.connectorTitle != null) {
                binding.transferConnectorText.text = if (row.connectorTravelMinutes != null) {
                    container.context.getString(
                        R.string.home_transfer_connector_travel_time,
                        row.connectorTitle,
                        row.connectorTravelMinutes,
                    )
                } else {
                    row.connectorTitle
                }
                val connectorColor = if (darkMode) Color.WHITE else tint
                binding.transferConnectorText.setTextColor(connectorColor)
                binding.transferConnectorIcon.imageTintList = ColorStateList.valueOf(connectorColor)
                binding.transferConnector.strokeColor = Color.argb(
                    if (darkMode) 153 else 46,
                    Color.red(tint),
                    Color.green(tint),
                    Color.blue(tint),
                )
                binding.transferConnector.alpha = 0f
                binding.transferConnector.scaleX = 0.96f
                binding.transferConnector.scaleY = 0.96f
            }
            container.addView(binding.root)
        }
        container.post {
            for (index in 0 until container.childCount) {
                val connector = container.getChildAt(index).findViewById<View>(R.id.transfer_connector)
                if (connector.visibility == View.VISIBLE) {
                    connector.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(180)
                        .start()
                }
            }
        }
    }
}
