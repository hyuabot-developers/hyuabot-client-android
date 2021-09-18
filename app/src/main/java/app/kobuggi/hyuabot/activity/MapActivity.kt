package app.kobuggi.hyuabot.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
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
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.CoroutineContext


class MapActivity : AppCompatActivity(), CoroutineScope, OnMapReadyCallback {
    private val markers = arrayListOf<MarkerItem>()
    private val databaseItems = arrayListOf<DatabaseItem>()

    private lateinit var job: Job
    private lateinit var database : AppDatabase
    private val mapView : MapView by lazy { findViewById(R.id.map_view) }
    private lateinit var map : NaverMap

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
                TedNaverClustering.with<MarkerItem>(this@MapActivity, map)
                    .items(markers)
                    .customMarker{
                        clusterItem: MarkerItem -> Marker(clusterItem.position).apply {
                            captionText = clusterItem.name
                            icon = OverlayImage.fromResource(R.drawable.marker_campus_building)
                        }
                    }
                    .make()
                // 지도 위치 이동
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.30016859443061, 126.83779653945606))
                map.moveCamera(cameraUpdate)
                map.moveCamera(CameraUpdate.zoomTo(15.0))
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
        map.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, false)
        map.symbolScale = 0F
        // 지도 다 로드 이후에 가져오기
        updateMarkersByCategory("building")
        // 줌 범위 설정
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        naverMap.minZoom = 14.0
        naverMap.maxZoom = 18.0
        // 현위치 버튼 기능
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false // 뷰 페이져에 가려져 이후 레이아웃에 정의 하였음.
        uiSetting.isZoomControlEnabled = false
    }
}