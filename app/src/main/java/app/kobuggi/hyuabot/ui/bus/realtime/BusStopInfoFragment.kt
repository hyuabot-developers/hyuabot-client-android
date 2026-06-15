package app.kobuggi.hyuabot.ui.bus.realtime

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogBusStopInfoBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BusStopInfoFragment @Inject constructor() : BottomSheetDialogFragment(), OnMapReadyCallback {
    private val binding by lazy { DialogBusStopInfoBinding.inflate(layoutInflater) }
    private val viewModel: BusStopInfoViewModel by viewModels()
    private val args: BusStopInfoFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val routeIDs = listOf(args.routeID, args.secondRouteID, args.thirdRouteID)
        viewModel.fetchData(args.stopID, routeIDs)
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.bus_stop_info_error), Toast.LENGTH_SHORT).show() }
        }
        viewModel.result.observe(viewLifecycleOwner) { busList ->
            if (busList.isEmpty()) return@observe
            val adapter = BusFirstLastAdapter(busList)
            binding.firstLastRecyclerView.apply {
                this.adapter = adapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            }
        }
        binding.stopMapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@BusStopInfoFragment)
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true
            behavior.isHideable = true
            behavior.skipCollapsed = true
        }
        return dialog
    }

    override fun onMapReady(map: GoogleMap) {
        viewModel.result.observe(viewLifecycleOwner) { busList ->
            if (busList.isEmpty()) return@observe
            val stopLat = busList.first().stop.latitude
            val stopLng = busList.first().stop.longitude
            val stopName = busList.first().stop.name
            val location = LatLng(stopLat, stopLng)
            map.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(stopName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
        }
    }

    override fun onStart() { super.onStart(); binding.stopMapView.onStart() }
    override fun onResume() { super.onResume(); binding.stopMapView.onResume() }
    override fun onPause() { super.onPause(); binding.stopMapView.onPause() }
    override fun onStop() { super.onStop(); binding.stopMapView.onStop() }
    override fun onDestroyView() {
        binding.stopMapView.onDestroy()
        super.onDestroyView()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.stopMapView.onSaveInstanceState(outState)
    }
    override fun onLowMemory() { super.onLowMemory(); binding.stopMapView.onLowMemory() }
}
