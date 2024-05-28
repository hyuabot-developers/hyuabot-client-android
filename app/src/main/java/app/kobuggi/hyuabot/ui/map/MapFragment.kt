package app.kobuggi.hyuabot.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment @Inject constructor() : Fragment(), OnMapReadyCallback {
    private val binding by lazy { FragmentMapBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MapViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@MapFragment)
        }
        return binding.root
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

    override fun onMapReady(map: GoogleMap) {
        map.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.29753535479288, 126.83544659517665), 16f))
            uiSettings.apply {
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
            setOnCameraIdleListener {
                val northLng = projection.visibleRegion.latLngBounds.northeast.latitude
                val southLng = projection.visibleRegion.latLngBounds.southwest.latitude
                val westLat = projection.visibleRegion.latLngBounds.southwest.longitude
                val eastLat = projection.visibleRegion.latLngBounds.northeast.longitude
                viewModel.fetchBuildings(northLng, southLng, westLat, eastLat)
            }
        }

        viewModel.buildings.observe(viewLifecycleOwner) { buildings ->
            map.apply {
                clear()
                buildings.forEach { building ->
                    addMarker(MarkerOptions().apply {
                        position(LatLng(building.latitude, building.longitude))
                        title(building.name)
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                    })
                }
                setOnMarkerClickListener {
                    it.showInfoWindow()
                    true
                }
            }
        }
    }
}
