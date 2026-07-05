package app.kobuggi.hyuabot.ui.cafeteria

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogCafeteriaInfoBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class CafeteriaInfoSheet : BottomSheetDialogFragment(), OnMapReadyCallback {
    private val binding by lazy { DialogCafeteriaInfoBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.toolbar.apply {
            title = requireArguments().getString(ARG_NAME).orEmpty()
            setOnMenuItemClickListener {
                dismiss()
                true
            }
        }
        binding.breakfastTime.text = getString(
            R.string.cafeteria_running_time_item_format,
            getString(R.string.cafeteria_tab_breakfast),
            requireArguments().getString(ARG_BREAKFAST).orDash()
        )
        binding.lunchTime.text = getString(
            R.string.cafeteria_running_time_item_format,
            getString(R.string.cafeteria_tab_lunch),
            requireArguments().getString(ARG_LUNCH).orDash()
        )
        binding.dinnerTime.text = getString(
            R.string.cafeteria_running_time_item_format,
            getString(R.string.cafeteria_tab_dinner),
            requireArguments().getString(ARG_DINNER).orDash()
        )
        binding.cafeteriaMapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@CafeteriaInfoSheet)
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

    override fun onMapReady(map: NaverMap) {
        val latitude = requireArguments().getDouble(ARG_LATITUDE)
        val longitude = requireArguments().getDouble(ARG_LONGITUDE)
        if (latitude == 0.0 && longitude == 0.0) return

        val location = LatLng(latitude, longitude)
        Marker().apply {
            position = location
            captionText = requireArguments().getString(ARG_NAME).orEmpty()
            icon = OverlayImage.fromResource(R.drawable.map_marker)
            this.map = map
        }
        map.moveCamera(CameraUpdate.scrollAndZoomTo(location, 17.0))
    }

    override fun onStart() { super.onStart(); binding.cafeteriaMapView.onStart() }
    override fun onResume() { super.onResume(); binding.cafeteriaMapView.onResume() }
    override fun onPause() { super.onPause(); binding.cafeteriaMapView.onPause() }
    override fun onStop() { super.onStop(); binding.cafeteriaMapView.onStop() }
    override fun onDestroyView() {
        binding.cafeteriaMapView.onDestroy()
        super.onDestroyView()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.cafeteriaMapView.onSaveInstanceState(outState)
    }
    override fun onLowMemory() { super.onLowMemory(); binding.cafeteriaMapView.onLowMemory() }

    private fun String?.orDash(): String = if (isNullOrBlank()) "-" else this

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"
        private const val ARG_BREAKFAST = "breakfast"
        private const val ARG_LUNCH = "lunch"
        private const val ARG_DINNER = "dinner"

        fun newInstance(cafeteria: CafeteriaPageQuery.Cafeterium, displayName: String): CafeteriaInfoSheet {
            return CafeteriaInfoSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, displayName.ifBlank { cafeteria.name })
                    putDouble(ARG_LATITUDE, cafeteria.latitude)
                    putDouble(ARG_LONGITUDE, cafeteria.longitude)
                    putString(ARG_BREAKFAST, cafeteria.runningTime.breakfast)
                    putString(ARG_LUNCH, cafeteria.runningTime.lunch)
                    putString(ARG_DINNER, cafeteria.runningTime.dinner)
                }
            }
        }
    }
}
