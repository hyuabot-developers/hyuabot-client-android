package app.kobuggi.hyuabot.ui.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class BuildingClusterRenderer(context: Context, map: GoogleMap, clusterManager: ClusterManager<BuildingMarkerItem>) : DefaultClusterRenderer<BuildingMarkerItem>(context, map, clusterManager) {
    init {
        clusterManager.renderer = this
    }

    override fun onBeforeClusterItemRendered(item: BuildingMarkerItem, markerOptions: MarkerOptions) {
        markerOptions.apply {
            icon(item.getIcon())
            title(item.getTitle())
            visible(true)
        }
    }
}
