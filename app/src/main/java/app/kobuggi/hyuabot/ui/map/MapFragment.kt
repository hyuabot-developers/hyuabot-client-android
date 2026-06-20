package app.kobuggi.hyuabot.ui.map
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.UIUtility

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMap.MapType
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
    private var isMapViewCreated = false
    private val buildingMarkerIconCache = mutableMapOf<String, OverlayImage>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewBundle = savedInstanceState?.getBundle(bundleKey)
        val searchResultAdapter = BuildingSearchAdapter(requireContext(), ::onClickSearchResult, emptyList())
        binding.mapView.let {
            it.onCreate(mapViewBundle)
            isMapViewCreated = true
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
        binding.campusCenterButton.setOnClickListener {
            AnalyticsManager.logSelect(AnalyticsItem.MAP_RECENTER)
            viewModel.searchRooms.value = false
            if (this::naverMap.isInitialized) {
                naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(CAMPUS_CENTER, 16.0))
            }
        }
        binding.currentLocationButton.setOnClickListener {
            moveToCurrentLocation()
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

    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        if (!this::naverMap.isInitialized) return
        val hasLocationPermission =
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasLocationPermission) {
            Toast.makeText(requireContext(), R.string.map_location_permission_required, Toast.LENGTH_SHORT).show()
            return
        }
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location == null) {
                    Toast.makeText(requireContext(), R.string.map_current_location_error, Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                viewModel.searchRooms.value = false
                naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(LatLng(location.latitude, location.longitude), 17.0))
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), R.string.map_current_location_error, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(bundleKey)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(bundleKey, mapViewBundle)
        }
        if (isMapViewCreated) {
            binding.mapView.onSaveInstanceState(mapViewBundle)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isMapViewCreated) {
            binding.mapView.onStart()
        }
    }
    override fun onStop() {
        super.onStop()
        if (isMapViewCreated) {
            binding.mapView.onStop()
        }
    }
    override fun onResume() {
        super.onResume()
        if (isMapViewCreated) {
            binding.mapView.onResume()
        }
    }
    override fun onPause() {
        super.onPause()
        if (isMapViewCreated) {
            binding.mapView.onPause()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        if (isMapViewCreated) {
            binding.mapView.onLowMemory()
        }
    }
    override fun onDestroyView() {
        if (isMapViewCreated) {
            runCatching {
                binding.mapView.onDestroy()
            }.onFailure {
                Log.w(TAG, "Failed to destroy map view", it)
            }
            isMapViewCreated = false
        }
        super.onDestroyView()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        map.apply {
            uiSettings.apply {
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
            mapType = MapType.Basic
            isNightModeEnabled = UIUtility.isDarkModeOn(resources)
            symbolScale = 0f
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
            naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(LatLng(room.latitude, room.longitude), 18.0))
            if (this::buildingClusterer.isInitialized) {
                buildingClusterer.clear()
            }
            searchMarker?.map = null
            searchMarker = Marker().apply {
                position = LatLng(room.latitude, room.longitude)
                captionText = ""
                subCaptionText = ""
                icon = buildingMarkerIcon(room.name)
                width = Marker.SIZE_AUTO
                height = Marker.SIZE_AUTO
                isHideCollidedSymbols = false
                map = naverMap
                setOnClickListener {
                    openBuildingUrlOrMap(room.url, position.latitude, position.longitude, room.name)
                    true
                }
            }
        }
    }

    private fun createBuildingClusterer(): Clusterer<BuildingClusterKey> {
        return Clusterer.Builder<BuildingClusterKey>()
            .leafMarkerUpdater { info: LeafMarkerInfo, marker: Marker ->
                val building = info.tag as? MapPageQuery.Building
                val key = info.key as? BuildingClusterKey
                val buildingName = key?.name ?: building?.name.orEmpty()
                marker.position = info.position
                marker.captionText = ""
                marker.subCaptionText = ""
                marker.icon = buildingMarkerIcon(buildingName)
                marker.width = Marker.SIZE_AUTO
                marker.height = Marker.SIZE_AUTO
                marker.isHideCollidedSymbols = false
                marker.setOnClickListener {
                    val selected = info.tag as? MapPageQuery.Building
                    val label = key?.name ?: selected?.name.orEmpty()
                    openBuildingUrlOrMap(
                        selected?.url,
                        marker.position.latitude,
                        marker.position.longitude,
                        label
                    )
                    true
                }
            }
            .clusterMarkerUpdater { info: ClusterMarkerInfo, marker: Marker ->
                marker.position = info.position
                marker.captionText = ""
                marker.subCaptionText = ""
                marker.icon = clusterIcon(info.size)
                marker.width = Marker.SIZE_AUTO
                marker.height = Marker.SIZE_AUTO
                marker.isHideCollidedSymbols = false
                marker.isHideCollidedCaptions = true
                marker.setOnClickListener {
                    val nextZoom = (naverMap.cameraPosition.zoom + 1.5).coerceAtMost(18.0)
                    naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(info.position, nextZoom))
                    true
                }
            }
            .build()
    }

    private fun clusterIcon(size: Int): OverlayImage {
        val diameter = when {
            size >= 100 -> 76
            size >= 10 -> 68
            else -> 60
        }
        val bitmap = createBitmap(diameter, diameter)
        val canvas = Canvas(bitmap)
        val center = diameter / 2f
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#0E4A84".toColorInt()
        }
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#1565A9".toColorInt()
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = when {
                size >= 100 -> 24f
                size >= 10 -> 26f
                else -> 28f
            }
            isFakeBoldText = true
        }
        canvas.drawCircle(center, center, center, outerPaint)
        canvas.drawCircle(center, center, center - 5f, innerPaint)
        val label = if (size > 99) "99+" else size.toString()
        val textY = center - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(label, center, textY, textPaint)
        return OverlayImage.fromBitmap(bitmap)
    }

    private fun buildingMarkerIcon(name: String): OverlayImage {
        val label = name.ifBlank { getString(R.string.menu_map) }
        val isDarkMode = UIUtility.isDarkModeOn(resources)
        val cacheKey = "${if (isDarkMode) "dark" else "light"}:$label"
        return buildingMarkerIconCache.getOrPut(cacheKey) {
            val markerSize = 52
            val labelHeight = 30
            val labelGap = 4
            val maxLabelWidth = 220
            val labelHorizontalPadding = 16
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = (if (isDarkMode) "#F5F8FF" else "#FFFFFF").toColorInt()
                textAlign = Paint.Align.CENTER
                textSize = 20f
                isFakeBoldText = true
            }
            val displayLabel = label.ellipsize(textPaint, maxLabelWidth - labelHorizontalPadding * 2)
            val textWidth = textPaint.measureText(displayLabel)
            val labelWidth = (textWidth + labelHorizontalPadding * 2).toInt().coerceIn(72, maxLabelWidth)
            val bitmapWidth = maxOf(labelWidth, markerSize)
            val bitmapHeight = labelHeight + labelGap + markerSize
            val bitmap = createBitmap(bitmapWidth, bitmapHeight)
            val canvas = Canvas(bitmap)
            val labelRect = RectF(
                (bitmapWidth - labelWidth) / 2f,
                0f,
                (bitmapWidth + labelWidth) / 2f,
                labelHeight.toFloat()
            )
            val labelBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = (if (isDarkMode) "#1D2533" else "#0E4A84").toColorInt()
            }
            val labelStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = (if (isDarkMode) "#4F6B8A" else "#06325A").toColorInt()
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(labelRect, 8f, 8f, labelBackgroundPaint)
            canvas.drawRoundRect(labelRect, 8f, 8f, labelStrokePaint)
            val textY = labelRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(displayLabel, labelRect.centerX(), textY, textPaint)

            ResourcesCompat.getDrawable(resources, R.drawable.map_marker, null)
                ?.mutate()
                ?.apply {
                    val markerLeft = (bitmapWidth - markerSize) / 2
                    val markerTop = labelHeight + labelGap
                    setBounds(markerLeft, markerTop, markerLeft + markerSize, markerTop + markerSize)
                    draw(canvas)
                }

            OverlayImage.fromBitmap(bitmap)
        }
    }

    private fun String.ellipsize(paint: Paint, maxWidth: Int): String {
        if (paint.measureText(this) <= maxWidth) return this
        var end = length
        while (end > 1 && paint.measureText(take(end) + "...") > maxWidth) {
            end--
        }
        return take(end) + "..."
    }

    private fun openBuildingUrlOrMap(url: String?, latitude: Double, longitude: Double, label: String) {
        if (!url.isNullOrBlank()) {
            val webUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else {
                "https://$url"
            }
            BuildingWebViewSheet.newInstance(label, webUrl).show(parentFragmentManager, "BuildingWebViewSheet")
            return
        }
        openExternalMap(latitude, longitude, label)
    }

    private fun openExternalMap(latitude: Double, longitude: Double, label: String) {
        val encodedLabel = Uri.encode(label.ifBlank { getString(R.string.menu_map) })
        val geoIntent = Intent(
            Intent.ACTION_VIEW,
            "geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)".toUri()
        )
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude".toUri()
        )
        try {
            startActivity(geoIntent)
        } catch (_: android.content.ActivityNotFoundException) {
            try {
                startActivity(webIntent)
            } catch (_: android.content.ActivityNotFoundException) {
                Toast.makeText(requireContext(), R.string.map_external_error, Toast.LENGTH_SHORT).show()
            }
        } catch (_: SecurityException) {
            Toast.makeText(requireContext(), R.string.map_external_error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "MapFragment"
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
