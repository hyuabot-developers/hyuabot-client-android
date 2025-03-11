package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.databinding.ItemShuttleRealtimeBinding
import app.kobuggi.hyuabot.ui.shuttle.via.ShuttleViaSheetDialog
import com.google.android.material.card.MaterialCardView
import java.time.LocalTime

class ShuttleRealtimeByTimeListAdapter(
    private val context: Context,
    private val shuttleRealtimeViewModel: ShuttleRealtimeViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val stopID: Int,
    private val childFragmentManager: FragmentManager,
    private var shuttleList: List<ShuttleRealtimePageQuery.Timetable>,
) : RecyclerView.Adapter<ShuttleRealtimeByTimeListAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemShuttleRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: ShuttleRealtimePageQuery.Timetable) {
            setStopText(context, binding.shuttleTypeText, binding.warningView, stopID, item)
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

    private fun setStopText(context: Context, textView: TextView, warningView: MaterialCardView, stopID: Int, item: ShuttleRealtimePageQuery.Timetable) {
        warningView.visibility = View.GONE
        when (stopID) {
            R.string.shuttle_tab_dormitory_out, R.string.shuttle_tab_shuttlecock_out -> {
                when(item.tag) {
                    "DH" -> {
                        textView.apply {
                            text = context.getString(R.string.shuttle_type_school_station)
                            setTextColor(context.getColor(R.color.hanyang_blue))
                        }
                    }
                    "DY" -> {
                        warningView.visibility = View.VISIBLE
                        textView.apply {
                            text = context.getString(R.string.shuttle_type_school_terminal)
                            setTextColor(context.getColor(R.color.hanyang_orange))
                        }
                    }
                    "DJ" -> {
                        textView.apply {
                            text = context.getString(R.string.shuttle_type_school_jungang_station)
                            setTextColor(context.getColor(R.color.hanyang_green))
                        }
                    }
                    "C" -> {
                        textView.apply {
                            text = context.getString(R.string.shuttle_type_school_circular)
                            setTextColor(context.getColor(R.color.primary_text))
                        }
                    }
                }
            }
            R.string.shuttle_tab_station -> {
                when (item.tag) {
                    "DH" -> {
                        if (item.route.endsWith("S")) {
                            textView.apply {
                                text = context.getString(R.string.shuttle_type_shuttlecock)
                                setTextColor(context.getColor(R.color.red_bus))
                            }
                        } else if (item.route.endsWith("D")) {
                            textView.apply {
                                text = context.getString(R.string.shuttle_type_dormitory)
                                setTextColor(context.getColor(R.color.hanyang_blue))
                            }
                        }
                    }
                    "DJ" -> {
                        textView.apply {
                            text = context.getString(R.string.shuttle_type_station_jungang_station)
                            setTextColor(context.getColor(R.color.hanyang_green))
                        }
                    }
                    "C" -> {
                        if (item.route.endsWith("S")) {
                            textView.apply {
                                text = context.getString(R.string.shuttle_type_station_circular_shuttlecock)
                                setTextColor(context.getColor(R.color.primary_text))
                            }
                        } else if (item.route.endsWith("D")) {
                            textView.apply {
                                text = context.getString(R.string.shuttle_type_station_circular_dormitory)
                                setTextColor(context.getColor(R.color.primary_text))
                            }
                        }
                    }
                }
            }
            R.string.shuttle_tab_terminal -> {
                if (item.route.endsWith("S")) {
                    textView.apply {
                        text = context.getString(R.string.shuttle_type_shuttlecock)
                        setTextColor(context.getColor(R.color.red_bus))
                    }
                } else if (item.route.endsWith("D")) {
                    textView.apply {
                        text = context.getString(R.string.shuttle_type_dormitory)
                        setTextColor(context.getColor(R.color.hanyang_blue))
                    }
                }
            }
            R.string.shuttle_tab_jungang_station -> {
                textView.apply {
                    text = context.getString(R.string.shuttle_type_dormitory)
                    setTextColor(context.getColor(R.color.hanyang_blue))
                }
            }
            R.string.shuttle_tab_shuttlecock_in -> {
                if (item.route.endsWith("S")) {
                    textView.apply {
                        text = context.getString(R.string.shuttle_type_shuttlecock_finishing)
                        setTextColor(context.getColor(R.color.red_bus))
                    }
                } else if (item.route.endsWith("D")) {
                    textView.apply {
                        text = context.getString(R.string.shuttle_type_dormitory)
                        setTextColor(context.getColor(R.color.hanyang_blue))
                    }
                }
            }
        }
    }
}
