package app.kobuggi.hyuabot.ui.shuttle.realtime
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemShuttleRealtimeBinding
import app.kobuggi.hyuabot.ui.shuttle.via.ShuttleViaSheetDialog
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

    inner class ViewHolder(private val binding: ItemShuttleRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        val darkMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: ShuttleRealtimePageQuery.Entry) {
            val isLastRun = item.seq in lastRunSeqs
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
                    binding.shuttleTimeText.text = formatTimeText(
                        context.getString(R.string.shuttle_time_type_2, (remainingTime.hour * 60 + remainingTime.minute).toString()),
                        isLastRun
                    )
                } else {
                    binding.shuttleTimeText.text = formatTimeText(
                        context.getString(
                            R.string.shuttle_time_type_1,
                            item.time.hour.toString().padStart(2, '0'),
                            item.time.minute.toString().padStart(2, '0')
                        ),
                        isLastRun
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
                val viaSheet = ShuttleViaSheetDialog(stopsOfTimetableByDestination = item.stops)
                viaSheet.show(childFragmentManager, "ShuttleViaSheetDialog")
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

    private fun formatTimeText(timeText: String, isLastRun: Boolean): String =
        if (isLastRun) "$timeText · ${context.getString(R.string.shuttle_last_run)}" else timeText
}
