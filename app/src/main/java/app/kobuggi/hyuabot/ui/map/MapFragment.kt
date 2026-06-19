package app.kobuggi.hyuabot.ui.map
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.MapPageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentMapBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.ClusteringKey
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment @Inject constructor() : Fragment(), OnMapReadyCallback {
    private val binding by lazy { FragmentMapBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MapViewModel>()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val bundleKey = "MapViewBundleKey"
    private lateinit var naverMap: NaverMap
    private lateinit var buildingClusterer: Clusterer<BuildingClusterKey>
    private var searchMarker: Marker? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewBundle = savedInstanceState?.getBundle(bundleKey)
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
            AnalyticsManager.logSelect(AnalyticsItem.MAP_RECENTER)
            viewModel.searchRooms.value = false
            if (this::naverMap.isInitialized) {
                naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(CAMPUS_CENTER, 16.0))
            }
        }
        viewModel.searchRooms.observe(viewLifecycleOwner) {
            binding.backToMoveButton.visibility = if (it) View.VISIBLE else View.GONE
            if (this::naverMap.isInitialized) {
                naverMap.uiSettings.apply {
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
        showCoachmarkOnce(userPreferencesRepository, Coachmarks.MAP) {
            listOf(
                CoachmarkStep(
                    { binding.searchBar },
                    R.string.coachmark_map_search_title, R.string.coachmark_map_search_desc
                ),
            )
        }
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(bundleKey)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(bundleKey, mapViewBundle)
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

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.apply {
            uiSettings.apply {
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
            moveCamera(CameraUpdate.scrollAndZoomTo(CAMPUS_CENTER, 16.0))
            buildingClusterer = createBuildingClusterer()
            buildingClusterer.map = this
            addOnCameraIdleListener {
                if (viewModel.searchRooms.value == true) {
                    return@addOnCameraIdleListener
                }
                val bounds = contentBounds
                viewModel.fetchBuildings(
                    bounds.northLatitude,
                    bounds.southLatitude,
                    bounds.westLongitude,
                    bounds.eastLongitude
                )
            }
        }
        viewModel.buildings.observe(viewLifecycleOwner) { buildings ->
            addClusterItems(buildings)
        }
    }

    private fun getMarkerIcon() = ResourcesCompat.getDrawable(resources, R.drawable.map_marker, null)
        ?.toBitmap(64, 64)
        ?.let { OverlayImage.fromBitmap(it) }

    private fun addClusterItems(buildings: List<MapPageQuery.Building>) {
        if (!this::naverMap.isInitialized) return
        searchMarker?.map = null
        searchMarker = null
        if (!this::buildingClusterer.isInitialized) return
        buildingClusterer.clear()
        buildingClusterer.addAll(
            buildings.associateWith { building ->
                BuildingClusterKey(
                    name = building.name,
                    latitude = building.latitude,
                    longitude = building.longitude
                )
            }.entries.associate { (building, key) -> key to building }
        )
    }

    private fun onClickSearchResult(room: RoomItem) {
        AnalyticsManager.logSelect(AnalyticsItem.MAP_SELECT_SEARCH_RESULT, type = AnalyticsContentType.LIST_ITEM, name = room.name)
        binding.searchView.hide()
        if (this::naverMap.isInitialized) {
            val markerIcon = getMarkerIcon() ?: return
            naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(LatLng(room.latitude, room.longitude), 18.0))
            if (this::buildingClusterer.isInitialized) {
                buildingClusterer.clear()
            }
            searchMarker?.map = null
            searchMarker = Marker().apply {
                position = LatLng(room.latitude, room.longitude)
                captionText = room.name
                subCaptionText = room.building
                icon = markerIcon
                map = naverMap
                setOnClickListener {
                    openExternalMap(position.latitude, position.longitude, captionText)
                    true
                }
            }
        }
    }

    private fun createBuildingClusterer(): Clusterer<BuildingClusterKey> {
        val markerIcon = getMarkerIcon()
        return Clusterer.Builder<BuildingClusterKey>()
            .leafMarkerUpdater { info: LeafMarkerInfo, marker: Marker ->
                val building = info.tag as? MapPageQuery.Building
                marker.position = info.position
                marker.captionText = building?.name.orEmpty()
                marker.icon = markerIcon ?: Marker.DEFAULT_ICON
                marker.setOnClickListener {
                    val selected = info.tag as? MapPageQuery.Building
                    openExternalMap(
                        marker.position.latitude,
                        marker.position.longitude,
                        selected?.name.orEmpty()
                    )
                    true
                }
            }
            .clusterMarkerUpdater { info: ClusterMarkerInfo, marker: Marker ->
                marker.position = info.position
                marker.captionText = info.size.toString()
                marker.setOnClickListener {
                    val nextZoom = (naverMap.cameraPosition.zoom + 1.5).coerceAtMost(18.0)
                    naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(info.position, nextZoom))
                    true
                }
            }
            .build()
    }

    private fun openExternalMap(latitude: Double, longitude: Double, label: String) {
        val encodedLabel = Uri.encode(label.ifBlank { getString(R.string.menu_map) })
        val intent = Intent(
            Intent.ACTION_VIEW,
            "geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)".toUri()
        )
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), R.string.map_external_error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val CAMPUS_CENTER = LatLng(37.29753535479288, 126.83544659517665)
    }

    private data class BuildingClusterKey(
        val name: String,
        val latitude: Double,
        val longitude: Double
    ) : ClusteringKey {
        private val latLng = LatLng(latitude, longitude)

        override fun getPosition(): LatLng = latLng
    }
}
