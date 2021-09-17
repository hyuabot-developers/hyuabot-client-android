package app.kobuggi.hyuabot.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.model.DatabaseItem
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ContactQueryResultListAdapter(list: ArrayList<DatabaseItem>) : RecyclerView.Adapter<ContactQueryResultListAdapter.ItemViewHolder>(){
    private val mList = list
    private lateinit var categoryMap : HashMap<String, String>

    init {
       when(Locale.getDefault().language){
           "ko" -> {
               categoryMap = hashMapOf(
                   "on campus" to "교내 기관",
                   "korean" to "한식",
                   "japanese" to "일식",
                   "chinese" to "중식",
                   "western" to "양식",
                   "fast food" to "분식 및 패스트푸드",
                   "chicken" to "치킨",
                   "pizza" to "피자",
                   "meat" to "육류",
                   "other food" to "기타 식당",
                   "cafe" to "카페",
                   "bakery" to "빵집",
                   "pub" to "주점"

               )
           }
       }
    }

    inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val contactName = itemView!!.findViewById<TextView>(R.id.contact_card_name)
        private val contactCategory = itemView!!.findViewById<TextView>(R.id.contact_card_category)
        private val contactPhoneNumber = itemView!!.findViewById<TextView>(R.id.contact_card_phone)

        @SuppressLint("SetTextI18n")
        fun bind(item: DatabaseItem){
            contactName.text = item.name
            contactCategory.text = categoryMap.getOrDefault(item.category, item.category)
            contactPhoneNumber.text = item.phoneNumber
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_contact, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun replaceTo(items: ArrayList<DatabaseItem>){
        mList.clear()
        mList.addAll(items)
    }
}