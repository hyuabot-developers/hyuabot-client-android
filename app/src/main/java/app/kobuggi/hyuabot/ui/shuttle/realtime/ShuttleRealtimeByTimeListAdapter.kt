package app.kobuggi.hyuabot.ui.shuttle.realtime

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

class ShuttleRealtimeByTimeListAdapter(
    private val context: Context,
    private val shuttleRealtimeViewModel: ShuttleRealtimeViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val stopID: Int,
    private val headerID: Int,
    private val childFragmentManager: FragmentManager,
    private var shuttleList: List<ShuttleRealtimePageQuery.Timetable>,
) : RecyclerView.Adapter<ShuttleRealtimeByTimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemShuttleRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: ShuttleRealtimePageQuery.Timetable) {
            if ((stopID == R.string.shuttle_tab_dormitory_out || stopID == R.string.shuttle_tab_shuttlecock_out)) {
                if (headerID == R.string.shuttle_header_bound_for_station || headerID == R.string.shuttle_header_bound_for_jungang_station) {
                    when (item.tag) {
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
                                setTextColor(context.getColor(R.color.hanyang_blue))
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
                    if (item.tag == "DY") {
                        binding.shuttleTypeText.apply {
                            visibility = ViewGroup.VISIBLE
                            text = context.getString(R.string.shuttle_type_direct)
                            setTextColor(context.getColor(R.color.red_bus))
                        }
                    } else if (item.tag == "C") {
                        binding.shuttleTypeText.apply {
                            visibility = ViewGroup.VISIBLE
                            text = context.getString(R.string.shuttle_type_circular)
                            setTextColor(context.getColor(R.color.hanyang_blue))
                        }
                    }
                }
            } else if (stopID == R.string.shuttle_tab_station) {
                if (headerID == R.string.shuttle_header_bound_for_dormitory) {
                    if (item.route.endsWith("S")) {
                        binding.shuttleTypeText.apply {
                            visibility = ViewGroup.VISIBLE
                            text = context.getString(R.string.shuttle_type_shuttlecock)
                            setTextColor(context.getColor(R.color.red_bus))
                        }
                    } else if (item.route.endsWith("D")) {
                        binding.shuttleTypeText.apply {
                            visibility = ViewGroup.VISIBLE
                            text = context.getString(R.string.shuttle_type_dormitory)
                            setTextColor(context.getColor(R.color.hanyang_blue))
                        }
                    }
                } else if (headerID == R.string.shuttle_header_bound_for_terminal) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_circular)
                        setTextColor(context.getColor(R.color.hanyang_blue))
                    }
                } else if (headerID == R.string.shuttle_header_bound_for_jungang_station) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_jungang)
                        setTextColor(context.getColor(R.color.hanyang_green))
                    }
                }
            } else if (stopID == R.string.shuttle_tab_terminal || stopID == R.string.shuttle_tab_jungang_station) {
                if (item.route.endsWith("S")) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_shuttlecock)
                        setTextColor(context.getColor(R.color.red_bus))
                    }
                } else if (item.route.endsWith("D")) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_dormitory)
                        setTextColor(context.getColor(R.color.hanyang_blue))
                    }
                }
            } else if (stopID == R.string.shuttle_tab_shuttlecock_in) {
                if (item.route.endsWith("S")) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_shuttlecock_finishing)
                        setTextColor(context.getColor(R.color.red_bus))
                    }
                } else if (item.route.endsWith("D")) {
                    binding.shuttleTypeText.apply {
                        visibility = ViewGroup.VISIBLE
                        text = context.getString(R.string.shuttle_type_dormitory)
                        setTextColor(context.getColor(R.color.hanyang_blue))
                    }
                }
            }

            val now = LocalTime.now()
            shuttleRealtimeViewModel.showDepartureTime.observe(lifecycleOwner) {
                val time = LocalTime.parse(item.time)
                if (!it) {
                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong() + 1)
                    binding.shuttleTimeText.text = context.getString(R.string.shuttle_time_type_2, (remainingTime.hour * 60 + remainingTime.minute).toString())
                } else {
                    binding.shuttleTimeText.text = context.getString(
                        R.string.shuttle_time_type_1,
                        time.hour.toString().padStart(2, '0'),
                        time.minute.toString().padStart(2, '0')
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
                val viaSheet = ShuttleViaSheetDialog(item.via)
                viaSheet.show(childFragmentManager, "ShuttleViaSheetDialog")
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

    fun updateData(newData: List<ShuttleRealtimePageQuery.Timetable>) {
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

    private fun setStopText(stopID: String) : String {
        return when(stopID) {
            "dormitory_o", "dormitory_i" -> context.getString(R.string.shuttle_tab_dormitory_out)
            "shuttlecock_o", "shuttlecock_i" -> context.getString(R.string.shuttle_tab_shuttlecock_out)
            "station" -> context.getString(R.string.shuttle_tab_station)
            "terminal" -> context.getString(R.string.shuttle_tab_terminal)
            "jungang_stn" -> context.getString(R.string.shuttle_tab_jungang_station)
            else -> ""
        }
    }
}
