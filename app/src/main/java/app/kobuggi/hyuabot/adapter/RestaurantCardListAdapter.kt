package app.kobuggi.hyuabot.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.model.MenuItem
import app.kobuggi.hyuabot.model.MenuListItem
import app.kobuggi.hyuabot.model.Restaurant
import app.kobuggi.hyuabot.model.RestaurantList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class RestaurantCardListAdapter(private val mContext: Context, private val mList: RestaurantList) : RecyclerView.Adapter<RestaurantCardListAdapter.ItemViewHolder>(){
    private val now = LocalDateTime.now()
    val menuList = arrayListOf<MenuListItem>()

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val foodCardTitle = itemView!!.findViewById<TextView>(R.id.food_card_title)
        private val restaurantMenuListView = itemView!!.findViewById<RecyclerView>(R.id.food_card_menu)
        private val arrowView = itemView!!.findViewById<ImageView>(R.id.food_card_arrow)

        private lateinit var currentMenuKey : String

        @SuppressLint("SetTextI18n")
        fun bind(item: Restaurant){
            foodCardTitle.text = item.Name
            menuList.clear()

            currentMenuKey = when(now.hour){
                in 0 .. 10 -> "조식"
                in 11 .. 15 -> "중식"
                else -> "석식"
            }
            
            if(currentMenuKey !in item.MenuList.keys){
                currentMenuKey = "중식"
            }

            for(key in listOf("조식", "중식", "석식", "중식/석식", "분식")){
                if (key in item.MenuList.keys){
                    for (menu in item.MenuList[key]!!){
                        menuList.add(MenuListItem(menu, key, key == currentMenuKey))
                    }
                }
            }
            var restaurantCardMenuAdapter = RestaurantCardMenuAdapter(menuList.filter { it.visible })
            restaurantMenuListView.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
            restaurantMenuListView.adapter = restaurantCardMenuAdapter
            arrowView.tag = R.drawable.ic_arrow_down


            arrowView.setOnClickListener {
                arrowView.setOnClickListener {
                    menuList.clear()
                    for(key in listOf("조식", "중식", "석식", "중식/석식", "분식")){
                        if (key in item.MenuList.keys){
                            for (menu in item.MenuList[key]!!){
                                menuList.add(MenuListItem(menu, key, key == currentMenuKey))
                            }
                        }
                    }

                    if(arrowView.tag == R.drawable.ic_arrow_down){
                        arrowView.setImageDrawable(ResourcesCompat.getDrawable(mContext.resources, R.drawable.ic_arrow_up, null))
                        arrowView.tag = R.drawable.ic_arrow_up

                        restaurantCardMenuAdapter = RestaurantCardMenuAdapter(menuList)
                        restaurantMenuListView.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
                        restaurantMenuListView.adapter = restaurantCardMenuAdapter
                    } else {
                        arrowView.setImageDrawable(ResourcesCompat.getDrawable(mContext.resources, R.drawable.ic_arrow_down, null))
                        arrowView.tag = R.drawable.ic_arrow_down

                        restaurantCardMenuAdapter = RestaurantCardMenuAdapter(menuList.filter { it.visible })
                        restaurantMenuListView.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
                        restaurantMenuListView.adapter = restaurantCardMenuAdapter
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_food, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.size
    }


}