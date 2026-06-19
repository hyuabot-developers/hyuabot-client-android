package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.SheetBusAlternativeStopBinding
import app.kobuggi.hyuabot.widget.ShuttleWidgetSupport
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class BusAlternativeStopSheet : BottomSheetDialogFragment(), OnMapReadyCallback {

    private val binding by lazy { SheetBusAlternativeStopBinding.inflate(layoutInflater) }
    private lateinit var shuttleStop: StopPoint
    private lateinit var busStop: StopPoint

    companion object {
        private const val ARG_SHUTTLE_STOP_NAME = "shuttle_stop_name"
        private const val ARG_SHUTTLE_STOP_LAT = "shuttle_stop_lat"
        private const val ARG_SHUTTLE_STOP_LNG = "shuttle_stop_lng"
        private const val ARG_BUS_STOP_NAME = "bus_stop_name"
        private const val ARG_BUS_STOP_LAT = "bus_stop_lat"
        private const val ARG_BUS_STOP_LNG = "bus_stop_lng"
        private const val MAP_CAMERA_PADDING = 260

        fun newInstance(
            shuttleStopName: String,
            shuttleStopLat: Double,
            shuttleStopLng: Double,
            busStopName: String,
            busStopLat: Double,
            busStopLng: Double
        ): BusAlternativeStopSheet {
            return BusAlternativeStopSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_SHUTTLE_STOP_NAME, shuttleStopName)
                    putDouble(ARG_SHUTTLE_STOP_LAT, shuttleStopLat)
                    putDouble(ARG_SHUTTLE_STOP_LNG, shuttleStopLng)
                    putString(ARG_BUS_STOP_NAME, busStopName)
                    putDouble(ARG_BUS_STOP_LAT, busStopLat)
                    putDouble(ARG_BUS_STOP_LNG, busStopLng)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = requireArguments()
        shuttleStop = StopPoint(
            args.getString(ARG_SHUTTLE_STOP_NAME, ""),
            args.getDouble(ARG_SHUTTLE_STOP_LAT),
            args.getDouble(ARG_SHUTTLE_STOP_LNG)
        )
        busStop = StopPoint(
            args.getString(ARG_BUS_STOP_NAME, ""),
            args.getDouble(ARG_BUS_STOP_LAT),
            args.getDouble(ARG_BUS_STOP_LNG)
        )

        binding.routeMapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@BusAlternativeStopSheet)
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true
            behavior.isHideable = true
            behavior.skipCollapsed = true
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: NaverMap) {
        val shuttleLatLng = LatLng(shuttleStop.lat, shuttleStop.lng)
        val busLatLng = LatLng(busStop.lat, busStop.lng)

        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isZoomControlEnabled = false
        map.uiSettings.isLocationButtonEnabled = false
        val shouldFitWithLocation = hasLocationPermission()
        if (shouldFitWithLocation) {
            focusMapOnStopsAndCurrentLocation(map, shuttleLatLng, busLatLng)
        }

        val shuttleMarker = Marker().apply {
            position = shuttleLatLng
            captionText = shuttleStop.name
            subCaptionText = getString(R.string.bus_alternative_shuttle_stop)
            icon = markerIcon(R.drawable.ic_shuttle_bus, Color.parseColor("#0E4A84"))
            anchor = Marker.DEFAULT_ANCHOR
            this.map = map
        }
        val busMarker = Marker().apply {
            position = busLatLng
            captionText = busStop.name
            subCaptionText = getString(R.string.bus_alternative_bus_stop)
            icon = markerIcon(R.drawable.ic_bus, Color.parseColor("#7DB928"))
            anchor = Marker.DEFAULT_ANCHOR
            this.map = map
        }
        val shuttleInfoWindow = infoWindow(shuttleStop.name, getString(R.string.bus_alternative_shuttle_stop))
        val busInfoWindow = infoWindow(busStop.name, getString(R.string.bus_alternative_bus_stop))
        shuttleMarker.setOnClickListener {
            shuttleInfoWindow.open(shuttleMarker)
            true
        }
        busMarker.setOnClickListener {
            busInfoWindow.open(busMarker)
            true
        }
        PathOverlay().apply {
            coords = listOf(shuttleLatLng, busLatLng)
            color = Color.parseColor("#0E4A84")
            width = 5
            this.map = map
        }
        Marker().apply {
            position = midpoint(shuttleLatLng, busLatLng)
            icon = vectorIcon(R.drawable.ic_arrow_right, Color.parseColor("#0E4A84"), 44)
            anchor = Marker.DEFAULT_ANCHOR
            angle = bearing(shuttleLatLng, busLatLng) - 90f
            this.map = map
        }

        binding.routeMapView.post {
            if (!shouldFitWithLocation) {
                focusMapOnStops(map, shuttleLatLng, busLatLng)
            }
            shuttleInfoWindow.open(shuttleMarker)
            busInfoWindow.open(busMarker)
        }
        map.addOnLoadListener {
            if (!shouldFitWithLocation) {
                focusMapOnStops(map, shuttleLatLng, busLatLng)
            }
            shuttleInfoWindow.open(shuttleMarker)
            busInfoWindow.open(busMarker)
        }
    }

    private fun focusMapOnStops(map: NaverMap, shuttleLatLng: LatLng, busLatLng: LatLng) {
        val bounds = LatLngBounds.from(listOf(shuttleLatLng, busLatLng))
        map.moveCamera(CameraUpdate.fitBounds(bounds, MAP_CAMERA_PADDING))
    }

    private fun focusMapOnStopsAndCurrentLocation(map: NaverMap, shuttleLatLng: LatLng, busLatLng: LatLng) {
        lifecycleScope.launch {
            val location = ShuttleWidgetSupport.getLocation(
                context = requireContext(),
                priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                maxAgeMillis = 15_000L,
                currentTimeoutMillis = 3_000L
            )
            val boundsPoints = mutableListOf(shuttleLatLng, busLatLng)
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                boundsPoints.add(currentLatLng)
                map.locationOverlay.apply {
                    position = currentLatLng
                    isVisible = true
                }
            }
            val bounds = LatLngBounds.from(boundsPoints)
            binding.routeMapView.post {
                map.moveCamera(CameraUpdate.fitBounds(bounds, MAP_CAMERA_PADDING))
            }
        }
    }

    private fun markerIcon(@DrawableRes iconRes: Int, @ColorInt color: Int): OverlayImage {
        val size = 88
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 5f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        })

        val icon = ContextCompat.getDrawable(requireContext(), iconRes)!!.mutate()
        icon.setTint(Color.WHITE)
        val inset = 22
        icon.setBounds(inset, inset, size - inset, size - inset)
        icon.draw(canvas)

        return OverlayImage.fromBitmap(bitmap)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun vectorIcon(@DrawableRes iconRes: Int, @ColorInt color: Int, size: Int): OverlayImage {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val icon = ContextCompat.getDrawable(requireContext(), iconRes)!!.mutate()
        icon.setTint(color)
        icon.setBounds(0, 0, size, size)
        icon.draw(canvas)
        return OverlayImage.fromBitmap(bitmap)
    }

    private fun infoWindow(title: String, subtitle: String): InfoWindow {
        return InfoWindow().apply {
            adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                override fun getText(infoWindow: InfoWindow): CharSequence {
                    return "$title\n$subtitle"
                }
            }
        }
    }

    private fun midpoint(from: LatLng, to: LatLng): LatLng {
        return LatLng((from.latitude + to.latitude) / 2, (from.longitude + to.longitude) / 2)
    }

    private fun bearing(from: LatLng, to: LatLng): Float {
        val fromLat = Math.toRadians(from.latitude)
        val toLat = Math.toRadians(to.latitude)
        val lngDiff = Math.toRadians(to.longitude - from.longitude)
        val y = sin(lngDiff) * cos(toLat)
        val x = cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(lngDiff)
        return ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
    }

    override fun onStart() {
        super.onStart()
        binding.routeMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.routeMapView.onResume()
    }

    override fun onPause() {
        binding.routeMapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.routeMapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.routeMapView.onDestroy()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.routeMapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.routeMapView.onLowMemory()
    }

    private data class StopPoint(
        val name: String,
        val lat: Double,
        val lng: Double
    )

}
