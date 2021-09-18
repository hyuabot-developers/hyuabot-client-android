package app.kobuggi.hyuabot.model

import com.naver.maps.geometry.LatLng
import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng

data class MarkerItem(val name: String, val category: String, val content: String?, val position : LatLng) : TedClusterItem{
    override fun getTedLatLng(): TedLatLng {
        return TedLatLng(position.latitude, position.longitude)
    }
}
