package app.kobuggi.hyuabot.ui.shuttle.realtime
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemShuttleRealtimeBinding
import app.kobuggi.hyuabot.ui.home.HomeSubwayTransferDestination
import app.kobuggi.hyuabot.ui.shuttle.via.ShuttleViaSheetDialog
import app.kobuggi.hyuabot.util.TransitRow
import app.kobuggi.hyuabot.util.buildShuttleConnectionRows
import java.time.LocalTime

class ShuttleRealtimeByDestinationListAdapter(
    private val context: Context,
    private val shuttleRealtimeViewModel: ShuttleRealtimeViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val stopID: Int,
    private val headerID: Int,
    private val childFragmentManager: FragmentManager,
    private var shuttleList: List<ShuttleRealtimePageQuery.Entry>,
    private val onAlarmClick: ((ShuttleRealtimePageQuery.Entry) -> Unit)? = null,
) : RecyclerView.Adapter<ShuttleRealtimeByDestinationListAdapter.ViewHolder>() {
    private var lastRunSeqs: Set<Int> = emptySet()
    private var expandedSeq: Int? = null
    private var transferData: ShuttleRealtimePageQuery.Data? = null
    private var showBusTransfer = true
    private var showSubwayTransfer = true
    private var subwayTransferDestination = HomeSubwayTransferDestination.SEOUL

    init {
        shuttleRealtimeViewModel.transfer.observe(lifecycleOwner) {
            transferData = it
            notifyDataSetChanged()
        }
        shuttleRealtimeViewModel.showBusTransfer.observe(lifecycleOwner) {
            showBusTransfer = it
            notifyDataSetChanged()
        }
        shuttleRealtimeViewModel.showSubwayTransfer.observe(lifecycleOwner) {
            showSubwayTransfer = it
            notifyDataSetChanged()
        }
        shuttleRealtimeViewModel.subwayTransferDestination.observe(lifecycleOwner) {
            subwayTransferDestination = it
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(private val binding: ItemShuttleRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        val darkMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: ShuttleRealtimePageQuery.Entry) {
            val transferRows = transferRows(item)
            val isExpanded = expandedSeq == item.seq && transferRows.isNotEmpty()
            binding.shuttleContent.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (isExpanded) R.color.app_selection_background else R.color.background,
                ),
            )
            binding.transferSelectionAccent.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.transferExpansionContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            if (isExpanded) {
                binding.transferSelectionAccent.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.hanyang_blue),
                )
                ShuttleTransferBinder.bindCompact(binding.transferExpansionContainer, transferRows)
            } else {
                binding.transferExpansionContainer.removeAllViews()
            }
            val isLastRun = item.seq in lastRunSeqs
            binding.lastRunBadge.visibility = if (isLastRun) View.VISIBLE else View.GONE
            binding.warningView.visibility = if (
                stopID == R.string.shuttle_tab_shuttlecock_out && item.route.tag == "DY"
            ) View.VISIBLE else View.GONE
            if ((stopID == R.string.shuttle_tab_dormitory_out || stopID == R.string.shuttle_tab_shuttlecock_out)) {
                if (headerID == R.string.shuttle_header_bound_for_station || headerID == R.string.shuttle_header_bound_for_jungang_station) {
                    when (item.route.tag) {
                        "DH" -> {
                            binding.shuttleTypeText.apply {
                                visibility = ViewGroup.VISIBLE
                                text = context.getString(R.string.shuttle_type_direct)
                                setTextColor(context.getColor(R.color.red_bus))
                            }
                        }
                        "C" -> {
                            binding.shuttleTypeText.apply {
                                visibility = ViewGroup.VISIBLE
                                text = context.getString(R.string.shuttle_type_circular)
                                setTextColor(context.getColor(if (darkMode) android.R.color.white else R.color.hanyang_blue))
                            }
                        }
                        "DJ" -> {
                            binding.shuttleTypeText.apply {
                                visibility = ViewGroup.VISIBLE
                                text = context.getString(R.string.shuttle_type_jungang)
                                setTextColor(context.getColor(R.color.hanyang_green))
                            }
                        }
                    }
                } else if (headerID == R.string.shuttle_header_bound_for_terminal) {
                    if (item.route.tag == "DY") {
                        binding.shuttleTypeText.apply {
                            visibility = ViewGroup.VISIBLE
                            text = context.getString(R.string.shuttle_type_direct)
                            setTextColor(context.getColor(R.color.red_bus))
                        }
                    } else if (item.route.tag == "C") {
                        binding.shuttleTypeText.apply {
                            visibility = ViewGroup.VISIBLE
                            text = context.getString(R.string.shuttle_type_circular)
                            setTextColor(context.getColor(if (darkMode) android.R.color.white else R.color.hanyang_blue))
                        }
                    }
                }
            } else if (stopID == R.string.shuttle_tab_station) {
                if (headerID == R.string.shuttle_header_bound_for_dormitory) {
                    if (item.route.name.endsWith("S")) {
                        if (item.route.tag == "C") {
                            binding.shuttleTypeText.apply {
                                visibility = ViewGroup.VISIBLE
                                text = context.getString(R.string.shuttle_type_shuttlecock_circular)
                                setTextColor(context.getColor(R.color.red_bus))
                            }
                        } else {
                            binding.shuttleTypeText.apply {
                                visibility = ViewGroup.VISIBLE
                                text = context.getString(R.string.shuttle_type_shuttlecock_direct)
                                setTextColor(context.getColor(R.color.red_bus))
                            }
                        }
                    } else if (item.route.name.endsWith("D")) {
                        when (item.route.tag) {
                            "C" -> {
                                binding.shuttleTypeText.apply {
                                    visibility = ViewGroup.VISIBLE
                                    text = context.getString(R.string.shuttle_type_dormitory_circular)
                                    setTextColor(context.getColor(if (darkMode) android.R.color.white else R.color.hanyang_blue))
                                }
                            }
                            "DJ" -> {
                                binding.shuttleTypeText.apply {
                                    visibility = ViewGroup.VISIBLE
                                    text = context.getString(R.string.shuttle_type_jungang)
                                    setTextColor(context.getColor(if (darkMode) android.R.color.white else R.color.hanyang_green))
                                }
                            }
                            else -> {
                                binding.shuttleTypeText.apply {
                                    visibility = ViewGroup.VISIBLE
                                    text = context.getString(R.string.shuttle_type_dormitory_direct)
                                    setTextColor(context.getColor(if (darkMode) android.R.color.white else R.color.hanyang_blue))
                                }
                            }
                        }
                    }
                } else if (headerID == R.string.shuttle_header_bound_for_terminal) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_circular)
                        setTextColor(context.getColor(if (darkMode) android.R.color.white else R.color.hanyang_blue))
                    }
                } else if (headerID == R.string.shuttle_header_bound_for_jungang_station) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_jungang)
                        setTextColor(context.getColor(R.color.hanyang_green))
                    }
                }
            } else if (stopID == R.string.shuttle_tab_terminal || stopID == R.string.shuttle_tab_jungang_station) {
                if (item.route.name.endsWith("S")) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_shuttlecock)
                        setTextColor(context.getColor(R.color.red_bus))
                    }
                } else if (item.route.name.endsWith("D")) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_dormitory)
                        setTextColor(context.getColor(if (darkMode) android.R.color.white else R.color.hanyang_blue))
                    }
                }
            } else if (stopID == R.string.shuttle_tab_shuttlecock_in) {
                if (item.route.name.endsWith("S")) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_shuttlecock_finishing)
                        setTextColor(context.getColor(R.color.red_bus))
                    }
                } else if (item.route.name.endsWith("D")) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_dormitory)
                        setTextColor(context.getColor(if (darkMode) android.R.color.white else R.color.hanyang_blue))
                    }
                }
            }

            val now = LocalTime.now()
            shuttleRealtimeViewModel.showDepartureTime.observe(lifecycleOwner) {
                if (!it) {
                    val remainingTime = item.time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong() + 1)
                    binding.shuttleTimeText.text = context.getString(
                        R.string.shuttle_time_type_2,
                        (remainingTime.hour * 60 + remainingTime.minute).toString()
                    )
                } else {
                    binding.shuttleTimeText.text = context.getString(
                        R.string.shuttle_time_type_1,
                        item.time.hour.toString().padStart(2, '0'),
                        item.time.minute.toString().padStart(2, '0')
                    )
                }
            }

            binding.shuttleItem.setOnTouchListener { _, event ->
                if (MotionEvent.ACTION_UP == event.action) {
                    shuttleRealtimeViewModel.setRemainingTimeVisibility(true)
                }
                false
            }

            binding.shuttleItem.setOnLongClickListener {
                shuttleRealtimeViewModel.setRemainingTimeVisibility(false)
                true
            }

            binding.shuttleItem.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SHUTTLE_SELECT_VIA_ROW, type = AnalyticsContentType.LIST_ITEM)
                if (transferRows.isEmpty()) {
                    val viaSheet = ShuttleViaSheetDialog(stopsOfTimetableByDestination = item.stops)
                    viaSheet.show(childFragmentManager, "ShuttleViaSheetDialog")
                } else {
                    toggleExpansion(item.seq)
                }
            }

            if (onAlarmClick != null) {
                binding.shuttleAlarmButton.visibility = ViewGroup.VISIBLE
                binding.shuttleAlarmButton.setOnClickListener {
                    onAlarmClick.invoke(item)
                }
            } else {
                binding.shuttleAlarmButton.visibility = ViewGroup.INVISIBLE
            }
        }

        private fun transferRows(item: ShuttleRealtimePageQuery.Entry): List<TransitRow> =
            buildShuttleConnectionRows(
                context = context,
                stopName = stopName(),
                destination = destinationName(),
                shuttle = item,
                data = transferData,
                showBusTransfer = showBusTransfer,
                showSubwayTransfer = showSubwayTransfer,
                subwayDestination = subwayTransferDestination,
            )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle_realtime, parent, false)
        return ViewHolder(ItemShuttleRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(shuttleList[position])
    }

    override fun getItemCount(): Int = shuttleList.size

    fun updateData(
        newData: List<ShuttleRealtimePageQuery.Entry>,
        lastRunSeqs: Set<Int> = emptySet(),
    ) {
        this.lastRunSeqs = lastRunSeqs
        if (expandedSeq != null && newData.none { it.seq == expandedSeq }) {
            expandedSeq = null
        }
        if (shuttleList.size > newData.size) {
            shuttleList = newData
            notifyItemRangeChanged(0, shuttleList.size)
            notifyItemRangeInserted(shuttleList.size, newData.size - shuttleList.size)
        } else if (shuttleList.size < newData.size) {
            shuttleList = newData
            notifyItemRangeChanged(0, newData.size)
            notifyItemRangeRemoved(newData.size, shuttleList.size - newData.size)
        } else {
            shuttleList = newData
            notifyItemRangeChanged(0, shuttleList.size)
        }
    }

    private fun toggleExpansion(seq: Int) {
        val previousSeq = expandedSeq
        expandedSeq = if (previousSeq == seq) null else seq
        previousSeq?.let { previous ->
            shuttleList.indexOfFirst { it.seq == previous }
                .takeIf { it >= 0 }
                ?.let(::notifyItemChanged)
        }
        if (expandedSeq != null) {
            shuttleList.indexOfFirst { it.seq == expandedSeq }
                .takeIf { it >= 0 }
                ?.let(::notifyItemChanged)
        }
    }

    private fun stopName(): String = when (stopID) {
        R.string.shuttle_tab_dormitory_out -> "dormitory_o"
        R.string.shuttle_tab_shuttlecock_out -> "shuttlecock_o"
        R.string.shuttle_tab_station -> "station"
        R.string.shuttle_tab_terminal -> "terminal"
        R.string.shuttle_tab_jungang_station -> "jungang_stn"
        else -> "shuttlecock_i"
    }

    private fun destinationName(): String = when (headerID) {
        R.string.shuttle_header_bound_for_station -> "STATION"
        R.string.shuttle_header_bound_for_terminal -> "TERMINAL"
        R.string.shuttle_header_bound_for_jungang_station -> "JUNGANG"
        else -> "CAMPUS"
    }
}
