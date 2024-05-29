package app.kobuggi.hyuabot.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.MapPageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment @Inject constructor() : Fragment(), OnMapReadyCallback {
    private val binding by lazy { FragmentMapBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MapViewModel>()
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private lateinit var clusterManager: ClusterManager<BuildingMarkerItem>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        binding.mapView.onCreate(mapViewBundle)
        binding.mapView.getMapAsync(this)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        binding.mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }
    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(map: GoogleMap) {
        map.apply {
            uiSettings.apply {
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.29753535479288, 126.83544659517665), 16f))
            clusterManager = BuildingClusterManager(requireContext(), map, viewModel)
            BuildingClusterRenderer(requireContext(), map, clusterManager)
            setOnCameraIdleListener(clusterManager)
            setOnMarkerClickListener(clusterManager)
        }
        map.setOnMapLoadedCallback {
            viewModel.buildings.observe(viewLifecycleOwner) { buildings ->
                addClusterItems(buildings)
            }
        }
    }

    private fun addClusterItems(buildings: List<MapPageQuery.Building>) {
        clusterManager.apply {
            clearItems()
            buildings.forEach { building ->
                clusterManager.addItem(BuildingMarkerItem(
                    building.name,
                    building.latitude,
                    building.longitude,
                    "",
                    BitmapDescriptorFactory.fromBitmap(ResourcesCompat.getDrawable(resources, R.drawable.map_marker, null)!!.toBitmap(64, 64))
                ))
            }
            cluster()
        }
    }
}
