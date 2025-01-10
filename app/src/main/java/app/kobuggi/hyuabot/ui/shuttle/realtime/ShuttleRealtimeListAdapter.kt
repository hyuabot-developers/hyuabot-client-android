package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemShuttleBinding
import java.time.LocalTime

class ShuttleRealtimeListAdapter(
    private val context: Context,
    private val shuttleRealtimeViewModel: ShuttleRealtimeViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val stopID: Int,
    private val headerID: Int,
    private var shuttleList: List<ShuttleRealtimePageQuery.Timetable>,
) : RecyclerView.Adapter<ShuttleRealtimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemShuttleBinding) : RecyclerView.ViewHolder(binding.root) {
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
            shuttleRealtimeViewModel.showRemainingTime.observe(lifecycleOwner) {
                val time = LocalTime.parse(item.time)
                if (it) {
                    val remainingTime = time.minusHours(now.hour.toLong()).minusMinutes(now.minute.toLong())
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
                binding.shuttleItemDetail.visibility = if (binding.shuttleItemDetail.visibility == ViewGroup.VISIBLE) ViewGroup.GONE else ViewGroup.VISIBLE
            }

            when(item.via.size) {
                3 -> {
                    binding.apply {
                        shuttleStopCircle23.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle24.visibility = ViewGroup.GONE
                        shuttleStopCircle34.visibility = ViewGroup.GONE
                        shuttleStopCircle25.visibility = ViewGroup.GONE
                        shuttleStopCircle35.visibility = ViewGroup.GONE
                        shuttleStopCircle45.visibility = ViewGroup.GONE
                        shuttleStopCircle26.visibility = ViewGroup.GONE
                        shuttleStopCircle36.visibility = ViewGroup.GONE
                        shuttleStopCircle46.visibility = ViewGroup.GONE
                        shuttleStopCircle56.visibility = ViewGroup.GONE
                        shuttleStopText1.text = setStopText(item.via[0].stop)
                        shuttleStopText23.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[1].stop)
                        }
                        shuttleStopText24.visibility = ViewGroup.GONE
                        shuttleStopText34.visibility = ViewGroup.GONE
                        shuttleStopText25.visibility = ViewGroup.GONE
                        shuttleStopText35.visibility = ViewGroup.GONE
                        shuttleStopText45.visibility = ViewGroup.GONE
                        shuttleStopText26.visibility = ViewGroup.GONE
                        shuttleStopText36.visibility = ViewGroup.GONE
                        shuttleStopText46.visibility = ViewGroup.GONE
                        shuttleStopText56.visibility = ViewGroup.GONE
                        shuttleStopText66.text = setStopText(item.via[2].stop)
                        shuttleTimeText1.text = item.time.substring(0, 5)
                        shuttleTimeText23.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[1].time.substring(0, 5)
                        }
                        shuttleTimeText24.visibility = ViewGroup.GONE
                        shuttleTimeText34.visibility = ViewGroup.GONE
                        shuttleTimeText25.visibility = ViewGroup.GONE
                        shuttleTimeText35.visibility = ViewGroup.GONE
                        shuttleTimeText45.visibility = ViewGroup.GONE
                        shuttleTimeText26.visibility = ViewGroup.GONE
                        shuttleTimeText36.visibility = ViewGroup.GONE
                        shuttleTimeText46.visibility = ViewGroup.GONE
                        shuttleTimeText56.visibility = ViewGroup.GONE
                        shuttleTimeText66.text = item.via[2].time.substring(0, 5)
                    }
                }
                4 -> {
                    binding.apply {
                        shuttleStopCircle23.visibility = ViewGroup.GONE
                        shuttleStopCircle24.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle34.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle25.visibility = ViewGroup.GONE
                        shuttleStopCircle35.visibility = ViewGroup.GONE
                        shuttleStopCircle45.visibility = ViewGroup.GONE
                        shuttleStopCircle26.visibility = ViewGroup.GONE
                        shuttleStopCircle36.visibility = ViewGroup.GONE
                        shuttleStopCircle46.visibility = ViewGroup.GONE
                        shuttleStopCircle56.visibility = ViewGroup.GONE
                        shuttleStopText1.text = setStopText(item.via[0].stop)
                        shuttleStopText23.visibility = ViewGroup.GONE
                        shuttleStopText24.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[1].stop)
                        }
                        shuttleStopText34.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[2].stop)
                        }
                        shuttleStopText25.visibility = ViewGroup.GONE
                        shuttleStopText35.visibility = ViewGroup.GONE
                        shuttleStopText45.visibility = ViewGroup.GONE
                        shuttleStopText26.visibility = ViewGroup.GONE
                        shuttleStopText36.visibility = ViewGroup.GONE
                        shuttleStopText46.visibility = ViewGroup.GONE
                        shuttleStopText56.visibility = ViewGroup.GONE
                        shuttleStopText66.text = setStopText(item.via[3].stop)
                        shuttleTimeText1.text = item.via[0].time.substring(0, 5)
                        shuttleTimeText23.visibility = ViewGroup.GONE
                        shuttleTimeText24.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[1].time.substring(0, 5)
                        }
                        shuttleTimeText34.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[2].time.substring(0, 5)
                        }
                        shuttleTimeText25.visibility = ViewGroup.GONE
                        shuttleTimeText35.visibility = ViewGroup.GONE
                        shuttleTimeText45.visibility = ViewGroup.GONE
                        shuttleTimeText26.visibility = ViewGroup.GONE
                        shuttleTimeText36.visibility = ViewGroup.GONE
                        shuttleTimeText46.visibility = ViewGroup.GONE
                        shuttleTimeText56.visibility = ViewGroup.GONE
                        shuttleTimeText66.text = item.via[3].time.substring(0, 5)
                    }
                }
                5 -> {
                    binding.apply {
                        shuttleStopCircle23.visibility = ViewGroup.GONE
                        shuttleStopCircle24.visibility = ViewGroup.GONE
                        shuttleStopCircle34.visibility = ViewGroup.GONE
                        shuttleStopCircle25.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle35.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle45.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle26.visibility = ViewGroup.GONE
                        shuttleStopCircle36.visibility = ViewGroup.GONE
                        shuttleStopCircle46.visibility = ViewGroup.GONE
                        shuttleStopCircle56.visibility = ViewGroup.GONE
                        shuttleStopText1.text = setStopText(item.via[0].stop)
                        shuttleStopText23.visibility = ViewGroup.GONE
                        shuttleStopText24.visibility = ViewGroup.GONE
                        shuttleStopText34.visibility = ViewGroup.GONE
                        shuttleStopText25.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[1].stop)
                        }
                        shuttleStopText35.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[2].stop)
                        }
                        shuttleStopText45.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[3].stop)
                        }
                        shuttleStopText26.visibility = ViewGroup.GONE
                        shuttleStopText36.visibility = ViewGroup.GONE
                        shuttleStopText46.visibility = ViewGroup.GONE
                        shuttleStopText56.visibility = ViewGroup.GONE
                        shuttleStopText66.text = setStopText(item.via[4].stop)
                        shuttleTimeText1.text = item.via[0].time.substring(0, 5)
                        shuttleTimeText23.visibility = ViewGroup.GONE
                        shuttleTimeText24.visibility = ViewGroup.GONE
                        shuttleTimeText34.visibility = ViewGroup.GONE
                        shuttleTimeText25.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[1].time.substring(0, 5)
                        }
                        shuttleTimeText35.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[2].time.substring(0, 5)
                        }
                        shuttleTimeText45.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[3].time.substring(0, 5)
                        }
                        shuttleTimeText26.visibility = ViewGroup.GONE
                        shuttleTimeText36.visibility = ViewGroup.GONE
                        shuttleTimeText46.visibility = ViewGroup.GONE
                        shuttleTimeText56.visibility = ViewGroup.GONE
                        shuttleTimeText66.text = item.via[4].time.substring(0, 5)
                    }
                }
                6 -> {
                    binding.apply {
                        shuttleStopCircle23.visibility = ViewGroup.GONE
                        shuttleStopCircle24.visibility = ViewGroup.GONE
                        shuttleStopCircle34.visibility = ViewGroup.GONE
                        shuttleStopCircle25.visibility = ViewGroup.GONE
                        shuttleStopCircle35.visibility = ViewGroup.GONE
                        shuttleStopCircle45.visibility = ViewGroup.GONE
                        shuttleStopCircle26.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle36.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle46.visibility = ViewGroup.VISIBLE
                        shuttleStopCircle56.visibility = ViewGroup.VISIBLE
                        shuttleStopText1.text = setStopText(item.via[0].stop)
                        shuttleStopText23.visibility = ViewGroup.GONE
                        shuttleStopText24.visibility = ViewGroup.GONE
                        shuttleStopText34.visibility = ViewGroup.GONE
                        shuttleStopText25.visibility = ViewGroup.GONE
                        shuttleStopText35.visibility = ViewGroup.GONE
                        shuttleStopText45.visibility = ViewGroup.GONE
                        shuttleStopText26.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[1].stop)
                        }
                        shuttleStopText36.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[2].stop)
                        }
                        shuttleStopText46.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[3].stop)
                        }
                        shuttleStopText56.apply {
                            visibility = ViewGroup.VISIBLE
                            text = setStopText(item.via[4].stop)
                        }
                        shuttleStopText66.text = setStopText(item.via[5].stop)
                        shuttleTimeText1.text = item.via[0].time.substring(0, 5)
                        shuttleTimeText23.visibility = ViewGroup.GONE
                        shuttleTimeText24.visibility = ViewGroup.GONE
                        shuttleTimeText34.visibility = ViewGroup.GONE
                        shuttleTimeText25.visibility = ViewGroup.GONE
                        shuttleTimeText35.visibility = ViewGroup.GONE
                        shuttleTimeText45.visibility = ViewGroup.GONE
                        shuttleTimeText26.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[1].time.substring(0, 5)
                        }
                        shuttleTimeText36.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[2].time.substring(0, 5)
                        }
                        shuttleTimeText46.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[3].time.substring(0, 5)
                        }
                        shuttleTimeText56.apply {
                            visibility = ViewGroup.VISIBLE
                            text = item.via[4].time.substring(0, 5)
                        }
                        shuttleTimeText66.text = item.via[5].time.substring(0, 5)
                    }
                }
                else -> {
                    binding.shuttleItemDetail.visibility = ViewGroup.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shuttle, parent, false)
        return ViewHolder(ItemShuttleBinding.bind(view))
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
