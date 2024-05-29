package app.kobuggi.hyuabot.ui.map

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class BuildingMarkerItem(
    private val name: String,
    private val latitude: Double,
    private val longitude: Double,
    private val snippet: String,
    private val icon: BitmapDescriptor
): ClusterItem {
    private val position: LatLng = LatLng(latitude, longitude)

    override fun getPosition(): LatLng = position
    override fun getTitle(): String = name
    override fun getSnippet(): String = snippet
    override fun getZIndex(): Float = 0f
    fun getIcon(): BitmapDescriptor = icon
}
