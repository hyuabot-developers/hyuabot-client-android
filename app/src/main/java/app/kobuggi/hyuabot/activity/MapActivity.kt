package app.kobuggi.hyuabot.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.room.Room
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.function.AppDatabase
import app.kobuggi.hyuabot.model.DatabaseItem
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint

import net.daum.mf.map.api.MapView
import kotlin.coroutines.CoroutineContext

class MapActivity : AppCompatActivity(), CoroutineScope {
    private val markers = arrayListOf<MapPOIItem>()
    private val databaseItems = arrayListOf<DatabaseItem>()

    private lateinit var job: Job
    private lateinit var database : AppDatabase
    private lateinit var mapView : MapView

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        job = Job()
        val toolbar = findViewById<Toolbar>(R.id.map_app_bar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        mapView = MapView(this)
        val mapViewContainer = findViewById<ViewGroup>(R.id.map_view)
        mapViewContainer.addView(mapView)

        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.300134067875675, 126.83786350205149), true)
        mapView.setZoomLevel(2, false)

        val categoryButton = findViewById<Button>(R.id.selected_category)
        val categoryButtonsContainer = findViewById<HorizontalScrollView>(R.id.select_category_scrollview)
        categoryButton.setOnClickListener {
            if(categoryButtonsContainer.isVisible){
                categoryButtonsContainer.visibility = View.INVISIBLE
            } else {
                categoryButtonsContainer.visibility = View.VISIBLE
            }
        }

        database = Room.databaseBuilder(this, AppDatabase::class.java, "app.db")
            .createFromAsset("app.db")
            .build()

        launch {
            databaseItems.addAll(database.databaseHelper()!!.getMarkersByCategory("building"))
            withContext(Main){
                for(item : DatabaseItem in databaseItems){
                    val marker = MapPOIItem()
                    marker.itemName = item.name
                    marker.tag = 0
                    marker.mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude!!, item.longitude!!)
                    marker.markerType = MapPOIItem.MarkerType.BluePin
                    markers.add(marker)
                    mapView.addPOIItem(marker)
                }
            }
        }

        val selectBuildingCategoryButton = findViewById<Button>(R.id.select_category_building)
        val selectKoreanCategoryButton = findViewById<Button>(R.id.select_category_korean)
        val selectJapaneseCategoryButton = findViewById<Button>(R.id.select_category_japanese)
        val selectChineseCategoryButton = findViewById<Button>(R.id.select_category_chinese)
        val selectWesternCategoryButton = findViewById<Button>(R.id.select_category_western)
        val selectVietnameseCategoryButton = findViewById<Button>(R.id.select_category_vietnamese)
        val selectFastFoodCategoryButton = findViewById<Button>(R.id.select_category_fast_food)
        val selectChickenCategoryButton = findViewById<Button>(R.id.select_category_chicken)
        val selectPizzaCategoryButton = findViewById<Button>(R.id.select_category_pizza)
        val selectMeatCategoryButton = findViewById<Button>(R.id.select_category_meat)
        val selectOtherFoodCategoryButton = findViewById<Button>(R.id.select_category_other_food)
        val selectBakeryCategoryButton = findViewById<Button>(R.id.select_category_bakery)
        val selectCafeCategoryButton = findViewById<Button>(R.id.select_category_cafe)
        val selectPubCategoryButton = findViewById<Button>(R.id.select_category_pub)

        val categories = arrayOf("building", "korean", "japanese", "chinese", "western", "vietnamese", "fast food", "chicken", "pizza", "meat", "other food", "bakery", "cafe", "pub")
        val buttons = arrayOf(
            selectBuildingCategoryButton, selectKoreanCategoryButton, selectJapaneseCategoryButton, selectChineseCategoryButton, selectWesternCategoryButton,
            selectVietnameseCategoryButton, selectFastFoodCategoryButton, selectChickenCategoryButton, selectPizzaCategoryButton, selectMeatCategoryButton,
            selectOtherFoodCategoryButton, selectBakeryCategoryButton, selectCafeCategoryButton, selectPubCategoryButton
        )

        for(i in buttons.indices){
            buttons[i].setOnClickListener {
                categoryButton.text = buttons[i].text
                updateMarkersByCategory(categories[i])
                categoryButtonsContainer.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateMarkersByCategory(category: String){
        launch {
            databaseItems.clear()
            databaseItems.addAll(database.databaseHelper()!!.getMarkersByCategory(category))
            withContext(Main){
                mapView.removeAllPOIItems()
                markers.clear()
                for(item : DatabaseItem in databaseItems){
                    val marker = MapPOIItem()
                    marker.itemName = item.name
                    marker.tag = 0
                    marker.mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude!!, item.longitude!!)
                    marker.markerType = MapPOIItem.MarkerType.BluePin
                    markers.add(marker)
                    mapView.addPOIItem(marker)
                }
            }
        }
    }
}