package app.kobuggi.hyuabot.map

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import ted.gun0912.clustering.TedMap
import ted.gun0912.clustering.geometry.TedCameraPosition
import ted.gun0912.clustering.geometry.TedLatLng
import ted.gun0912.clustering.geometry.TedLatLngBounds

class TedNaverMap(private val naverMap: NaverMap) : TedMap<Marker, TedNaverMarker, OverlayImage> {
    override fun getCameraPosition(): TedCameraPosition {
        val cameraPosition = naverMap.cameraPosition
        val tedLatLng = TedLatLng(cameraPosition.target.latitude, cameraPosition.target.longitude)
        return TedCameraPosition(
            tedLatLng,
            cameraPosition.zoom,
            cameraPosition.tilt,
            cameraPosition.bearing
        )
    }

    override fun addOnCameraIdleListener(onCameraIdleListener: (tedCameraPosition: TedCameraPosition) -> Unit) {
        naverMap.addOnCameraIdleListener { onCameraIdleListener.invoke(getCameraPosition()) }
    }

    override fun addMarker(tedNaverMarker: TedNaverMarker) {
        tedNaverMarker.marker.map = naverMap
    }

    override fun removeMarker(tedNaverMarker: TedNaverMarker) {
        tedNaverMarker.marker.map = null
    }

    override fun getVisibleLatLngBounds(): TedLatLngBounds =
        TedLatLngBounds().apply {
            val bounds = naverMap.contentBounds
            southWest = TedLatLng(
                bounds.southWest.latitude,
                bounds.southWest.longitude
            )
            northEast = TedLatLng(
                bounds.northEast.latitude,
                bounds.northEast.longitude
            )
        }


    override fun moveToCenter(tedLatLng: TedLatLng) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(tedLatLng.latitude, tedLatLng.longitude)).animate(CameraAnimation.Linear)
        naverMap.moveCamera(cameraUpdate)
    }


    override fun getMarker(): TedNaverMarker {
        return getMarker(Marker())
    }

    override fun getMarker(marker: Marker): TedNaverMarker {
        return TedNaverMarker(marker)
    }

    override fun addMarkerClickListener(
        tedNaverMarker: TedNaverMarker,
        action: (TedNaverMarker) -> Unit
    ) {
        tedNaverMarker.marker.setOnClickListener {
            action.invoke(tedNaverMarker)
            true
        }
    }
}