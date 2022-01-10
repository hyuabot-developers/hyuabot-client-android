package app.kobuggi.hyuabot.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.activity.*
import app.kobuggi.hyuabot.databinding.ActivityMainBinding
import app.kobuggi.hyuabot.ui.BindingActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.time.*


class MainActivity : BindingActivity<ActivityMainBinding>() {
    override fun getLayoutResourceID() = R.layout.activity_main

    // 버튼 정보 Array
    private val buttonIDList = listOf(
        R.id.menu_shuttle_button, R.id.menu_bus_button, R.id.menu_subway_button, R.id.menu_food_button,
        R.id.menu_library_button, R.id.menu_contact_button, R.id.menu_map_button, R.id.menu_calendar_button
    )
    private val buttonLabelList = listOf(
        R.string.shuttle, R.string.bus, R.string.subway, R.string.food,
        R.string.library, R.string.contact, R.string.map, R.string.calendar
    )
    private val buttonIconList = listOf(
        R.drawable.menu_shuttle, R.drawable.menu_bus, R.drawable.menu_metro, R.drawable.menu_restaurant,
        R.drawable.menu_library, R.drawable.menu_contact, R.drawable.menu_map, R.drawable.menu_calendar
    )
    private val newActivitiesWhenButtonClicked = listOf(
        ShuttleActivity::class.java, BusActivity::class.java, SubwayActivity::class.java, RestaurantActivity::class.java,
        ReadingRoomActivity::class.java, ContactActivity::class.java, MapActivity::class.java, CalendarActivity::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.shuttleViewModel = getViewModel()
        binding.restaurantMenuViewModel = getViewModel()
        binding.lifecycleOwner = this

        val expandMenu = findViewById<LinearLayout>(R.id.expanded_menu)
        val expandMenuButton = findViewById<TextView>(R.id.expanded_menu_button)
        expandMenuButton.setOnClickListener {
            if(expandMenu.visibility == View.VISIBLE){
                expandMenu.visibility = View.GONE
                expandMenuButton.text = "전체 보기"
            } else {
                expandMenu.visibility = View.VISIBLE
                expandMenuButton.text = "줄이기"
            }
        }

        createButtons()
        loadNativeAd()
        openBirthDayDialog()
        binding.shuttleViewModel.fetchShuttleArrival()
        binding.restaurantMenuViewModel.fetchRestaurantMenu()
    }

    private fun openBirthDayDialog() {
        val pref = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        val lastOpenedYear = pref.getInt("birthDayOpened", 0)
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

        var dialogMessage = "안녕하세요.\n휴아봇 개발자 경원여객3102입니다.\n"
        dialogMessage += "오늘(12/12)은 제 생일입니다.\n\n"
        dialogMessage += "부족하지만 많이 사용하시는 학우 여러분에게\n"
        dialogMessage += "감사의 말씀드립니다."

        if (now.monthValue == 12 && now.dayOfMonth == 12 && lastOpenedYear != now.year){
            val dialogBuilder = AlertDialog.Builder(this)
            val dialogLayout = R.layout.dialog_with_do_not_show_checkbox
            val dialogView = LayoutInflater.from(this).inflate(dialogLayout,null)
            val dialogCheckBox = dialogView.findViewById<CheckBox>(R.id.do_not_show_checkbox)
            dialogBuilder.setTitle("12/12 공지입니다.")
            dialogBuilder.setMessage(dialogMessage)
            dialogBuilder.setView(dialogView)

            dialogBuilder.setPositiveButton("확인") { dialogInterface, _ ->
                if (dialogCheckBox.isChecked){
                    pref.edit().putInt("birthDayOpened", now.year).apply()
                }
                dialogInterface.dismiss()
            }
            dialogBuilder.create().show()
        }
    }

    // 버튼 라벨, 아이콘 맵핑
    private fun createButtons(){
        for (i in 0..7){
            val button = findViewById<RelativeLayout>(buttonIDList[i])
            button.findViewById<ImageView>(R.id.button_icon).setImageDrawable(ResourcesCompat.getDrawable(resources, buttonIconList[i], null))
            button.findViewById<TextView>(R.id.button_label).text = resources.getString(buttonLabelList[i])
            button.setOnClickListener {
                val newActivity = Intent(this, newActivitiesWhenButtonClicked[i])
                startActivity(newActivity)
            }
        }
    }
}