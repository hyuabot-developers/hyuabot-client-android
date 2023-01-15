package app.kobuggi.hyuabot.component.card.subway

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.CardSubwayRealtimeBinding
import app.kobuggi.hyuabot.model.subway.SubwayRealtimeListResponse
import app.kobuggi.hyuabot.model.subway.SubwayTimetableListResponse

class RealtimeRouteCardAdapter (
    private val context: Context,
    private var bookmarkIndex: Int,
    private val onClickBookmark: (Int) -> Unit,
    private val onClickTimetableButton: (String, String) -> Unit
) : RecyclerView.Adapter<RealtimeRouteCardAdapter.ViewHolder>() {
    private val cardList: List<CardItem> = arrayListOf(
        CardItem(R.string.line_no_4, "K449", listOf(R.string.heading_up, R.string.heading_down)),
        CardItem(R.string.line_suinbundang, "K251", listOf(R.string.heading_up, R.string.heading_down)),
        CardItem(R.string.transfer_oido_station, "", listOf(R.string.heading_ansan, R.string.heading_incheon)),
    )
    inner class ViewHolder(private val binding: CardSubwayRealtimeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int, cardItem: CardItem) {
            val resources = context.resources
            binding.subwayRouteName.text = resources.getString(cardItem.title)
            binding.addBookmarkButton.setOnClickListener {
                onClickBookmark(bindingAdapterPosition)
            }
            if (index == bookmarkIndex) {
                binding.addBookmarkButton.setImageResource(R.drawable.ic_bookmark_checked)
            } else {
                binding.addBookmarkButton.setImageResource(R.drawable.ic_bookmark_unchecked)
            }
            val adapter = RealtimeHeadingItemAdapter(context, cardItem, onClickTimetableButton)
            binding.subwayRealtimeRouteRecyclerView.adapter = adapter
            binding.subwayRealtimeRouteRecyclerView.layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_subway_realtime, parent, false)
        return ViewHolder(CardSubwayRealtimeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, cardList[position])
    }

    override fun getItemCount(): Int = cardList.size

    fun updateBookmarkIndex(bookmarkIndex: Int) {
        this.bookmarkIndex = bookmarkIndex
        notifyItemRangeChanged(0, 3)
    }

    fun updateK449Data(realtime: SubwayRealtimeListResponse, timetable: SubwayTimetableListResponse) {
        cardList[0].realtimeList = realtime
        cardList[0].timetableList = timetable
        notifyItemChanged(0)
    }

    fun updateK251Data(realtime: SubwayRealtimeListResponse, timetable: SubwayTimetableListResponse) {
        cardList[1].realtimeList = realtime
        cardList[1].timetableList = timetable
        notifyItemChanged(1)
    }

    fun updateK456Data(realtime: SubwayRealtimeListResponse, timetable: SubwayTimetableListResponse) {
        cardList[2].realtimeList = realtime
        cardList[2].timetableList = timetable
        notifyItemChanged(2)
    }

    fun updateK258Data(realtime: SubwayRealtimeListResponse, timetable: SubwayTimetableListResponse) {
        cardList[2].transferRealtimeList = realtime
        cardList[2].transferTimetableList = timetable
        notifyItemChanged(2)
    }
}