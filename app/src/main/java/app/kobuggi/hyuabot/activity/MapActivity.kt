package app.kobuggi.hyuabot.activity

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.room.Room
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.function.AppDatabase
import app.kobuggi.hyuabot.function.DatabaseHelper
import app.kobuggi.hyuabot.model.DatabaseItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint

import net.daum.mf.map.api.MapView

class MapActivity : AppCompatActivity() {
    private val markers = arrayListOf<MapPOIItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val toolbar = findViewById<Toolbar>(R.id.map_app_bar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val mapView = MapView(this)
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

        val database = Room.databaseBuilder(this, AppDatabase::class.java, "app.db")
            .createFromAsset("app.db")
            .build()

        val getMarkersThread = GlobalScope.launch {
            markers.clear()
            mapView.removeAllPOIItems()
            for(item : DatabaseItem in database.databaseHelper()!!.getMarkersByCategory("building")){
                val marker = MapPOIItem()
                marker.itemName = item.name
                marker.tag = 0
                marker.mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude!!, item.longitude!!)
                marker.markerType = MapPOIItem.MarkerType.BluePin
                markers.add(marker)
                mapView.addPOIItem(marker)
            }
        }

        runBlocking { getMarkersThread.join() }
    }
}