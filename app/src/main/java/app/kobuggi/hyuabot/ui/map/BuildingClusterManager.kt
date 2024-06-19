package app.kobuggi.hyuabot.ui.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager

class BuildingClusterManager<T : ClusterItem?>(context: Context, private val map: GoogleMap, private val viewModel: MapViewModel) : ClusterManager<T>(context, map) {
    override fun onCameraIdle() {
        super.onCameraIdle()
        if (viewModel.searchRooms.value == true) {
            return
        }
        map.projection.visibleRegion.latLngBounds.let {
            viewModel.fetchBuildings(it.northeast.latitude, it.southwest.latitude, it.southwest.longitude, it.northeast.longitude)
        }
    }
}
