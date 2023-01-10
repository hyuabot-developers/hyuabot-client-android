package app.kobuggi.hyuabot.component.card.cafeteria

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.GlobalApplication
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.CardCafeteriaBinding
import app.kobuggi.hyuabot.databinding.CardSubwayRealtimeBinding
import app.kobuggi.hyuabot.model.cafeteria.RestaurantItemResponse
import app.kobuggi.hyuabot.model.subway.SubwayRealtimeListResponse
import app.kobuggi.hyuabot.model.subway.SubwayTimetableListResponse

class RestaurantCardAdapter (private val context: Context) : RecyclerView.Adapter<RestaurantCardAdapter.ViewHolder>() {
    private val cardList: List<CardItem> = arrayListOf(
        CardItem(R.string.breakfast, listOf()),
        CardItem(R.string.lunch, listOf()),
        CardItem(R.string.dinner, listOf())
    )
    inner class ViewHolder(private val binding: CardCafeteriaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cardItem: CardItem) {
            val resources = GlobalApplication.getAppResources()
            binding.cafeteriaTimeName.text = resources.getString(cardItem.timetypeID)
            if (cardItem.restaurantList.filter { it.menu.isNotEmpty() }.isNotEmpty()) {
                binding.cafeteriaRestaurantRecyclerView.adapter = RestaurantItemAdapter(context, cardItem.restaurantList.filter { it.menu.isNotEmpty() })
                binding.cafeteriaRestaurantRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.cafeteriaRestaurantRecyclerView.visibility = View.VISIBLE
                binding.cafeteriaNoData.visibility = View.GONE
            } else {
                binding.cafeteriaRestaurantRecyclerView.visibility = View.GONE
                binding.cafeteriaNoData.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_cafeteria, parent, false)
        return ViewHolder(CardCafeteriaBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cardList[position])
    }

    override fun getItemCount(): Int = cardList.size
    fun updateData(index: Int, data: List<RestaurantItemResponse>) {
        cardList[index].restaurantList = data
        notifyItemChanged(index)
    }
}