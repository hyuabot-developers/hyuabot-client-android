package app.kobuggi.hyuabot.activity

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import app.kobuggi.hyuabot.R
import com.kakao.util.maps.helper.Utility
import net.daum.mf.map.api.CameraUpdate
import net.daum.mf.map.api.MapPoint

import net.daum.mf.map.api.MapView
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MapActivity : AppCompatActivity() {
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
    }
}