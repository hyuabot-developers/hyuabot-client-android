package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogShuttleStopBinding
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
class ShuttleStopDialog @Inject constructor() : BottomSheetDialogFragment(), OnMapReadyCallback {
    private val binding by lazy { DialogShuttleStopBinding.inflate(layoutInflater) }
    private val viewModel: ShuttleStopDialogViewModel by viewModels()
    private val args : ShuttleStopDialogArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val stopID = args.stopID
        viewModel.fetchData(
            when(stopID) {
                R.string.shuttle_tab_dormitory_out -> "dormitory_o"
                R.string.shuttle_tab_shuttlecock_out -> "shuttlecock_o"
                R.string.shuttle_tab_station -> "station"
                R.string.shuttle_tab_terminal -> "terminal"
                R.string.shuttle_tab_jungang_station -> "jungang_stn"
                R.string.shuttle_tab_shuttlecock_in -> "shuttlecock_i"
                else -> "dormitory_o"
            }
        )
        viewModel.queryError.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), getString(R.string.shuttle_stop_info_error), Toast.LENGTH_SHORT).show() }
        }
        binding.apply {
            toolbar.apply {
                title = getString(stopID)
                setOnMenuItemClickListener {
                    dismiss()
                    true
                }
            }
            stopMapView.apply {
                onCreate(savedInstanceState)
                getMapAsync(this@ShuttleStopDialog)
            }
        }

        when (stopID) {
            R.string.shuttle_tab_dormitory_out, R.string.shuttle_tab_shuttlecock_out -> {
                binding.apply {
                    shuttleFirstLastTimeStationLayout.visibility = View.VISIBLE
                    shuttleFirstLastTimeDormitoryLayout.visibility = View.GONE
                    shuttleFirstLastTimeTerminalLayout.visibility = View.VISIBLE
                    shuttleFirstLastTimeJungangStationLayout.visibility = View.VISIBLE
                }
            }
            R.string.shuttle_tab_station -> {
                binding.apply {
                    shuttleFirstLastTimeStationLayout.visibility = View.GONE
                    shuttleFirstLastTimeDormitoryLayout.visibility = View.VISIBLE
                    shuttleFirstLastTimeTerminalLayout.visibility = View.VISIBLE
                    shuttleFirstLastTimeJungangStationLayout.visibility = View.VISIBLE
                }
            }
            else -> {
                binding.apply {
                    shuttleFirstLastTimeStationLayout.visibility = View.GONE
                    shuttleFirstLastTimeDormitoryLayout.visibility = View.VISIBLE
                    shuttleFirstLastTimeTerminalLayout.visibility = View.GONE
                    shuttleFirstLastTimeJungangStationLayout.visibility = View.GONE
                }
            }
        }

        viewModel.departureList.observe(viewLifecycleOwner) {
            when (stopID) {
                R.string.shuttle_tab_dormitory_out, R.string.shuttle_tab_shuttlecock_out -> {
                    val departureList = it.sortedBy { item -> item.time }
                    val weekdaysStationDepartureList = departureList.filter { item -> item.weekdays && (item.tag == "C" || item.tag == "DH" || item.tag == "DJ") }
                    val weekendsStationDepartureList = departureList.filter { item -> !item.weekdays && (item.tag == "C" || item.tag == "DH" || item.tag == "DJ") }
                    val weekdaysTerminalDepartureList = departureList.filter { item -> item.weekdays && (item.tag == "C" || item.tag == "DY") }
                    val weekendsTerminalDepartureList = departureList.filter { item -> !item.weekdays && (item.tag == "C" || item.tag == "DY") }
                    val weekdaysJungangStationDepartureList = departureList.filter { item -> item.weekdays && item.tag == "DJ" }
                    val weekendsJungangStationDepartureList = departureList.filter { item -> !item.weekdays && item.tag == "DJ" }
                    binding.apply {
                        if (weekdaysStationDepartureList.isNotEmpty()) {
                            shuttleForStationFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysStationDepartureList[0].time.split(":")[0], weekdaysStationDepartureList[0].time.split(":")[1])
                            shuttleForStationLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysStationDepartureList[weekdaysStationDepartureList.size - 1].time.split(":")[0], weekdaysStationDepartureList[weekdaysStationDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekendsStationDepartureList.isNotEmpty()) {
                            shuttleForStationFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsStationDepartureList[0].time.split(":")[0], weekendsStationDepartureList[0].time.split(":")[1])
                            shuttleForStationLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsStationDepartureList[weekendsStationDepartureList.size - 1].time.split(":")[0], weekendsStationDepartureList[weekendsStationDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekdaysTerminalDepartureList.isNotEmpty()) {
                            shuttleForTerminalFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysTerminalDepartureList[0].time.split(":")[0], weekdaysTerminalDepartureList[0].time.split(":")[1])
                            shuttleForTerminalLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysTerminalDepartureList[weekdaysTerminalDepartureList.size - 1].time.split(":")[0], weekdaysTerminalDepartureList[weekdaysTerminalDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekendsTerminalDepartureList.isNotEmpty()) {
                            shuttleForTerminalFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsTerminalDepartureList[0].time.split(":")[0], weekendsTerminalDepartureList[0].time.split(":")[1])
                            shuttleForTerminalLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsTerminalDepartureList[weekendsTerminalDepartureList.size - 1].time.split(":")[0], weekendsTerminalDepartureList[weekendsTerminalDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekdaysJungangStationDepartureList.isNotEmpty()) {
                            shuttleForJungangStationFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysJungangStationDepartureList[0].time.split(":")[0], weekdaysJungangStationDepartureList[0].time.split(":")[1])
                            shuttleForJungangStationLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysJungangStationDepartureList[weekdaysJungangStationDepartureList.size - 1].time.split(":")[0], weekdaysJungangStationDepartureList[weekdaysJungangStationDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekendsJungangStationDepartureList.isNotEmpty()) {
                            shuttleForJungangStationFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsJungangStationDepartureList[0].time.split(":")[0], weekendsJungangStationDepartureList[0].time.split(":")[1])
                            shuttleForJungangStationLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsJungangStationDepartureList[weekendsJungangStationDepartureList.size - 1].time.split(":")[0], weekendsJungangStationDepartureList[weekendsJungangStationDepartureList.size - 1].time.split(":")[1])
                        }
                    }
                }
                R.string.shuttle_tab_station -> {
                    val departureList = it.sortedBy { item -> item.time }
                    val weekdaysDormitoryDepartureList = departureList.filter { item -> item.weekdays }
                    val weekendsDormitoryDepartureList = departureList.filter { item -> !item.weekdays }
                    val weekdaysTerminalDepartureList = departureList.filter { item -> item.weekdays && item.tag == "C" }
                    val weekendsTerminalDepartureList = departureList.filter { item -> !item.weekdays && item.tag == "C" }
                    val weekdaysJungangStationDepartureList = departureList.filter { item -> item.weekdays && item.tag == "DJ" }
                    val weekendsJungangStationDepartureList = departureList.filter { item -> !item.weekdays && item.tag == "DJ" }
                    binding.apply {
                        if (weekdaysDormitoryDepartureList.isNotEmpty()) {
                            shuttleForDormitoryFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysDormitoryDepartureList[0].time.split(":")[0], weekdaysDormitoryDepartureList[0].time.split(":")[1])
                            shuttleForDormitoryLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysDormitoryDepartureList[weekdaysDormitoryDepartureList.size - 1].time.split(":")[0], weekdaysDormitoryDepartureList[weekdaysDormitoryDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekendsDormitoryDepartureList.isNotEmpty()) {
                            shuttleForDormitoryFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsDormitoryDepartureList[0].time.split(":")[0], weekendsDormitoryDepartureList[0].time.split(":")[1])
                            shuttleForDormitoryLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsDormitoryDepartureList[weekendsDormitoryDepartureList.size - 1].time.split(":")[0], weekendsDormitoryDepartureList[weekendsDormitoryDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekdaysTerminalDepartureList.isNotEmpty()) {
                            shuttleForTerminalFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysTerminalDepartureList[0].time.split(":")[0], weekdaysTerminalDepartureList[0].time.split(":")[1])
                            shuttleForTerminalLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysTerminalDepartureList[weekdaysTerminalDepartureList.size - 1].time.split(":")[0], weekdaysTerminalDepartureList[weekdaysTerminalDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekendsTerminalDepartureList.isNotEmpty()) {
                            shuttleForTerminalFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsTerminalDepartureList[0].time.split(":")[0], weekendsTerminalDepartureList[0].time.split(":")[1])
                            shuttleForTerminalLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsTerminalDepartureList[weekendsTerminalDepartureList.size - 1].time.split(":")[0], weekendsTerminalDepartureList[weekendsTerminalDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekdaysJungangStationDepartureList.isNotEmpty()) {
                            shuttleForJungangStationFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysJungangStationDepartureList[0].time.split(":")[0], weekdaysJungangStationDepartureList[0].time.split(":")[1])
                            shuttleForJungangStationLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysJungangStationDepartureList[weekdaysJungangStationDepartureList.size - 1].time.split(":")[0], weekdaysJungangStationDepartureList[weekdaysJungangStationDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekendsJungangStationDepartureList.isNotEmpty()) {
                            shuttleForJungangStationFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsJungangStationDepartureList[0].time.split(":")[0], weekendsJungangStationDepartureList[0].time.split(":")[1])
                            shuttleForJungangStationLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsJungangStationDepartureList[weekendsJungangStationDepartureList.size - 1].time.split(":")[0], weekendsJungangStationDepartureList[weekendsJungangStationDepartureList.size - 1].time.split(":")[1])
                        }
                    }
                }
                else -> {
                    val departureList = it.sortedBy { item -> item.time }
                    val weekdaysDepartureList = departureList.filter { item -> item.weekdays }
                    val weekendsDepartureList = departureList.filter { item -> !item.weekdays }
                    binding.apply {
                        if (weekdaysDepartureList.isNotEmpty()) {
                            shuttleForDormitoryFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysDepartureList[0].time.split(":")[0], weekdaysDepartureList[0].time.split(":")[1])
                            shuttleForDormitoryLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysDepartureList[weekdaysDepartureList.size - 1].time.split(":")[0], weekdaysDepartureList[weekdaysDepartureList.size - 1].time.split(":")[1])
                        }
                        if (weekendsDepartureList.isNotEmpty()) {
                            shuttleForDormitoryFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsDepartureList[0].time.split(":")[0], weekendsDepartureList[0].time.split(":")[1])
                            shuttleForDormitoryLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsDepartureList[weekendsDepartureList.size - 1].time.split(":")[0], weekendsDepartureList[weekendsDepartureList.size - 1].time.split(":")[1])
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
             behavior.isDraggable = false
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        binding.stopMapView.onStart()
    }
    override fun onStop() {
        super.onStop()
        binding.stopMapView.onStop()
    }
    override fun onResume() {
        super.onResume()
        binding.stopMapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        binding.stopMapView.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        binding.stopMapView.onLowMemory()
    }
    override fun onDestroy() {
        binding.stopMapView.onDestroy()
        super.onDestroy()
    }

    override fun onMapReady(map: GoogleMap) {
        map.apply {
            uiSettings.apply {
                isZoomControlsEnabled = false
                isZoomGesturesEnabled = false
                isScrollGesturesEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
        }
        viewModel.result.observe(viewLifecycleOwner) {
            if (it != null) {
                map.apply {
                    moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 17f))
                    addMarker(MarkerOptions().apply {
                        position(LatLng(it.latitude, it.longitude))
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker))
                    })
                }
            }
        }
    }
}
