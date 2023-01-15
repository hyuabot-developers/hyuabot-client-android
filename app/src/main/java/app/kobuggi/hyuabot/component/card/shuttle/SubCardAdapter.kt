package app.kobuggi.hyuabot.component.card.shuttle

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.CardShuttleSubCardBinding
import app.kobuggi.hyuabot.model.bus.BusRouteStartStopItem
import app.kobuggi.hyuabot.model.bus.BusStopRouteItem
import app.kobuggi.hyuabot.model.shuttle.ArrivalListStopItem
import app.kobuggi.hyuabot.model.subway.SubwayRealtimeListResponse

class SubCardAdapter(private val context: Context) : RecyclerView.Adapter<SubCardAdapter.ViewHolder>() {
    private val subCardDataList: List<SubCardData> = listOf(
        SubCardData(R.string.shuttle_sub_card_subway_title, R.string.shuttle_sub_card_subway_subtitle, R.string.shuttle_sub_card_subway_left_title, R.string.shuttle_sub_card_subway_right_title,
            SubCardItemList(SubwayRealtimeListResponse(listOf(), listOf()), SubwayRealtimeListResponse(listOf(), listOf()))),
        SubCardData(R.string.shuttle_sub_card_suwon_title, R.string.shuttle_sub_card_suwon_subtitle, R.string.shuttle_sub_card_suwon_left_title, R.string.shuttle_sub_card_suwon_right_title,
            SubCardItemList(
                SubwayRealtimeListResponse(listOf(), listOf()),
                BusStopRouteItem(0, "", BusRouteStartStopItem(0, "", listOf()), listOf())
            ),
        ),
        SubCardData(R.string.shuttle_sub_card_sangnoksu_title, R.string.shuttle_sub_card_sangnoksu_subtitle, R.string.shuttle_sub_card_sangnoksu_left_title, R.string.shuttle_sub_card_sangnoksu_right_title,
            SubCardItemList(
                ArrivalListStopItem("", listOf()),
                BusStopRouteItem(0, "", BusRouteStartStopItem(0, "", listOf()), listOf())
            ),
        ),
        SubCardData(R.string.shuttle_sub_card_gwangmyeong_title, R.string.shuttle_sub_card_gwangmyeong_subtitle, R.string.shuttle_sub_card_gwangmyeong_left_title, R.string.shuttle_sub_card_gwangmyeong_right_title,
            SubCardItemList(
                BusStopRouteItem(0, "", BusRouteStartStopItem(0, "", listOf()), listOf()),
                BusStopRouteItem(0, "", BusRouteStartStopItem(0, "", listOf()), listOf())
            ),
        ),
    )
    inner class ViewHolder(private val binding: CardShuttleSubCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int, subCardData: SubCardData) {
            val resources = context.resources
            binding.shuttleSubCardTitle.text = resources.getString(subCardData.title)
            binding.shuttleSubCardSubtitle.text = resources.getString(subCardData.subtitle)
            binding.shuttleSubCardLeftTitle.text = resources.getString(subCardData.leftTitle)
            binding.shuttleSubCardRightTitle.text = resources.getString(subCardData.rightTitle)
            val leftAdapter = SubCardItemAdapter(context, index, subCardData.data.left, View.TEXT_ALIGNMENT_VIEW_START)
            val rightAdapter = SubCardItemAdapter(context, index, subCardData.data.right, View.TEXT_ALIGNMENT_VIEW_END)
            if (leftAdapter.itemCount > 0){
                binding.shuttleSubCardLeftRecyclerView.adapter = leftAdapter
                binding.shuttleSubCardLeftRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.shuttleSubCardLeftRecyclerView.visibility = View.VISIBLE
                binding.shuttleSubCardLeftNoData.visibility = View.GONE
            } else {
                binding.shuttleSubCardLeftRecyclerView.visibility = View.GONE
                binding.shuttleSubCardLeftNoData.visibility = View.VISIBLE
            }
            if (rightAdapter.itemCount > 0){
                binding.shuttleSubCardRightRecyclerView.adapter = rightAdapter
                binding.shuttleSubCardRightRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.shuttleSubCardRightRecyclerView.visibility = View.VISIBLE
                binding.shuttleSubCardRightNoData.visibility = View.GONE
            } else {
                binding.shuttleSubCardRightRecyclerView.visibility = View.GONE
                binding.shuttleSubCardRightNoData.visibility = View.VISIBLE
            }
            binding.shuttleSubCardLeftRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.shuttleSubCardRightRecyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_shuttle_sub_card, parent, false)
        return ViewHolder(CardShuttleSubCardBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, subCardDataList[position])
    }

    override fun getItemCount(): Int = subCardDataList.size

    fun updateSubwayArrival(subwayRealtimeArrival: SubwayRealtimeListResponse) {
        subCardDataList[0].data.left = subwayRealtimeArrival
        subCardDataList[0].data.right = subwayRealtimeArrival
        subCardDataList[1].data.left = subwayRealtimeArrival
        notifyItemRangeChanged(0, 2)
    }

    fun updateBusArrivalToSuwon(busArrival: BusStopRouteItem){
        subCardDataList[1].data.right = busArrival
        notifyItemChanged(1)
    }

    fun updateBusArrivalFromSangnoksu(busArrival: BusStopRouteItem){
        subCardDataList[2].data.right = busArrival
        notifyItemChanged(2)
    }

    fun updateBusArrivalFromGwangmyeong(busArrival: BusStopRouteItem){
        subCardDataList[3].data.left = busArrival
        notifyItemChanged(3)
    }

    fun updateBusArrivalToGwangmyeong(busArrival: BusStopRouteItem){
        subCardDataList[3].data.right = busArrival
        notifyItemChanged(3)
    }

    fun updateStopList(arrivalItemList: List<ArrivalListStopItem>) {
        subCardDataList[2].data.left = arrivalItemList.find { it.stopName == "station" } ?: ArrivalListStopItem("", listOf())
        notifyItemChanged(2)
    }
}