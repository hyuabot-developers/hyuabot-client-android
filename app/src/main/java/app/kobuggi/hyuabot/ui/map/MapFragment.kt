package app.kobuggi.hyuabot.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.MapPageQuery
import app.kobuggi.hyuabot.MapPageSearchQuery
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
    private lateinit var googleMap: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        val searchResultAdapter = BuildingSearchAdapter(requireContext(), ::onClickSearchResult, emptyList())
        binding.mapView.let {
            it.onCreate(mapViewBundle)
            it.getMapAsync(this)
        }
        binding.searchRecyclerView.apply {
            setHasFixedSize(true)
            adapter = searchResultAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
        binding.searchView
            .editText
            .apply {
                addTextChangedListener { editable ->
                    if (editable.isNullOrBlank()) {
                        searchResultAdapter.updateData(emptyList())
                    } else {
                        viewModel.searchRooms(editable.toString())
                    }
                }
            }
        binding.backToMoveButton.setOnClickListener {
            viewModel.searchRooms.value = false
            if (this::googleMap.isInitialized) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.29753535479288, 126.83544659517665), 16f))
            }
        }
        viewModel.searchRooms.observe(viewLifecycleOwner) {
            binding.backToMoveButton.visibility = if (it) View.VISIBLE else View.GONE
            if (this::googleMap.isInitialized) {
                googleMap.uiSettings.apply {
                    isScrollGesturesEnabled = !it
                    isZoomGesturesEnabled = !it
                }
            }
        }
        viewModel.rooms.observe(viewLifecycleOwner) { rooms ->
            searchResultAdapter.updateData(rooms)
        }
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.map_building_error), Toast.LENGTH_SHORT).show() }
        }
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
        googleMap = map
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

    private fun onClickSearchResult(room: MapPageSearchQuery.Room) {
        binding.searchView.hide()
        if (this::googleMap.isInitialized) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(room.latitude, room.longitude), 18f))
            clusterManager.apply {
                clearItems()
                addItem(BuildingMarkerItem(
                    room.name,
                    room.latitude,
                    room.longitude,
                    room.buildingName,
                    BitmapDescriptorFactory.fromBitmap(ResourcesCompat.getDrawable(resources, R.drawable.map_marker, null)!!.toBitmap(64, 64))
                ))
                cluster()
            }
        }
    }
}
