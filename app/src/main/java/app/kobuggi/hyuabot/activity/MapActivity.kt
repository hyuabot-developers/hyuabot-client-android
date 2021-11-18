package app.kobuggi.hyuabot.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.room.Room
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.function.AppDatabase
import app.kobuggi.hyuabot.map.TedNaverClustering
import app.kobuggi.hyuabot.model.DatabaseItem
import app.kobuggi.hyuabot.model.MarkerItem
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.CoroutineContext


class MapActivity : AppCompatActivity(), CoroutineScope, OnMapReadyCallback {
    private val markers = arrayListOf<MarkerItem>()
    private val databaseItems = arrayListOf<DatabaseItem>()
    private lateinit var infoWindow : InfoWindow

    private lateinit var job: Job
    private lateinit var database : AppDatabase
    private val mapView : MapView by lazy { findViewById(R.id.map_view) }
    private lateinit var map : NaverMap
    lateinit var tedNaverClustering: TedNaverClustering<MarkerItem>

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        job = Job()
        val toolbar = findViewById<Toolbar>(R.id.map_app_bar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

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
            markers.clear()
            for(item : DatabaseItem in databaseItems){
                val marker = MarkerItem(item.name, item.category, item.description, LatLng(item.latitude!!, item.longitude!!))
                markers.add(marker)
            }


            withContext(Main){
                tedNaverClustering.clearItems()
                tedNaverClustering.addItems(markers)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(naverMap: NaverMap) {
        map = naverMap
        infoWindow = InfoWindow()
        // 지도 다 로드 이후에 가져오기
        updateMarkersByCategory("building")
        naverMap.minZoom = 14.0
        naverMap.maxZoom = 18.0
        // 현위치 버튼 기능
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false // 뷰 페이져에 가려져 이후 레이아웃에 정의 하였음.
        uiSetting.isZoomControlEnabled = false

        tedNaverClustering = TedNaverClustering.with<MarkerItem>(this, naverMap)
            .customMarker{
                Marker().apply {
                    icon = if (it.category == "building"){
                        OverlayImage.fromResource(R.drawable.marker_school)
                    } else if (it.category == "bakery"){
                        OverlayImage.fromResource(R.drawable.marker_bakery)
                    } else if(it.category == "cafe"){
                        OverlayImage.fromResource(R.drawable.marker_cafe)
                    } else if(it.category == "pub"){
                        OverlayImage.fromResource(R.drawable.marker_pub)
                    } else {
                        OverlayImage.fromResource(R.drawable.marker_restaurant)
                    }
                    width = 100
                    height = 100
                    captionText = it.name
                    captionColor = getColor(R.color.hanyang_primary)
                    isHideCollidedSymbols = true
                    isHideCollidedMarkers = true
                    isHideCollidedCaptions = true
                }
            }
            .markerClickListener { markerItem ->
                val description = markerItem.content.toString()
                if (description.startsWith("건물 번호 :")){
                    Toast.makeText(
                        this,
                        description,
                        Toast.LENGTH_SHORT
                    ).show()
                } else{
                    Toast.makeText(
                        this,
                        "메뉴 : $description",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .minClusterSize(10)
            .make()
        // 지도 위치 이동
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.30016859443061, 126.83779653945606))
        map.moveCamera(cameraUpdate)
        map.moveCamera(CameraUpdate.zoomTo(16.0))
    }
}