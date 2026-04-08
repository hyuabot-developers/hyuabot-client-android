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
                    val departureForStation = it.first { item -> item.destination == "STATION" }.entries.sortedBy { item -> item.time }
                    val departureForTerminal = it.first { item -> item.destination == "TERMINAL" }.entries.sortedBy { item -> item.time }
                    val departureForJungangStation = it.first { item -> item.destination == "JUNGANG" }.entries.sortedBy { item -> item.time }
                    val weekdaysStationDepartureList = departureForStation.filter { item -> item.weekday }
                    val weekendsStationDepartureList = departureForStation.filter { item -> !item.weekday }
                    val weekdaysTerminalDepartureList = departureForTerminal.filter { item -> item.weekday }
                    val weekendsTerminalDepartureList = departureForTerminal.filter { item -> !item.weekday }
                    val weekdaysJungangStationDepartureList = departureForJungangStation.filter { item -> item.weekday }
                    val weekendsJungangStationDepartureList = departureForJungangStation.filter { item -> !item.weekday }
                    binding.apply {
                        if (weekdaysStationDepartureList.isNotEmpty()) {
                            shuttleForStationFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysStationDepartureList[0].time.hour.toString().padStart(2, '0'), weekdaysStationDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForStationLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysStationDepartureList[weekdaysStationDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekdaysStationDepartureList[weekdaysStationDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekendsStationDepartureList.isNotEmpty()) {
                            shuttleForStationFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsStationDepartureList[0].time.hour.toString().padStart(2, '0'), weekendsStationDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForStationLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsStationDepartureList[weekendsStationDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekendsStationDepartureList[weekendsStationDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekdaysTerminalDepartureList.isNotEmpty()) {
                            shuttleForTerminalFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysTerminalDepartureList[0].time.hour.toString().padStart(2, '0'), weekdaysTerminalDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForTerminalLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysTerminalDepartureList[weekdaysTerminalDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekdaysTerminalDepartureList[weekdaysTerminalDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekendsTerminalDepartureList.isNotEmpty()) {
                            shuttleForTerminalFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsTerminalDepartureList[0].time.hour.toString().padStart(2, '0'), weekendsTerminalDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForTerminalLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsTerminalDepartureList[weekendsTerminalDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekendsTerminalDepartureList[weekendsTerminalDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekdaysJungangStationDepartureList.isNotEmpty()) {
                            shuttleForJungangStationFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysJungangStationDepartureList[0].time.hour.toString().padStart(2, '0'), weekdaysJungangStationDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForJungangStationLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysJungangStationDepartureList[weekdaysJungangStationDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekdaysJungangStationDepartureList[weekdaysJungangStationDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekendsJungangStationDepartureList.isNotEmpty()) {
                            shuttleForJungangStationFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsJungangStationDepartureList[0].time.hour.toString().padStart(2, '0'), weekendsJungangStationDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForJungangStationLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsJungangStationDepartureList[weekendsJungangStationDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekendsJungangStationDepartureList[weekendsJungangStationDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                    }
                }
                R.string.shuttle_tab_station -> {
                    val departureForCampus = it.first { item -> item.destination == "CAMPUS" }.entries.sortedBy { item -> item.time }
                    val departureForTerminal = it.first { item -> item.destination == "TERMINAL" }.entries.sortedBy { item -> item.time }
                    val departureForJungangStation = it.first { item -> item.destination == "JUNGANG" }.entries.sortedBy { item -> item.time }
                    val weekdaysDormitoryDepartureList = departureForCampus.filter { item -> item.weekday }
                    val weekendsDormitoryDepartureList = departureForCampus.filter { item -> !item.weekday }
                    val weekdaysTerminalDepartureList = departureForTerminal.filter { item -> item.weekday }
                    val weekendsTerminalDepartureList = departureForTerminal.filter { item -> !item.weekday }
                    val weekdaysJungangStationDepartureList = departureForJungangStation.filter { item -> item.weekday }
                    val weekendsJungangStationDepartureList = departureForJungangStation.filter { item -> !item.weekday }
                    binding.apply {
                        if (weekdaysDormitoryDepartureList.isNotEmpty()) {
                            shuttleForDormitoryFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysDormitoryDepartureList[0].time.hour.toString().padStart(2, '0'), weekdaysDormitoryDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForDormitoryLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysDormitoryDepartureList[weekdaysDormitoryDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekdaysDormitoryDepartureList[weekdaysDormitoryDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekendsDormitoryDepartureList.isNotEmpty()) {
                            shuttleForDormitoryFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsDormitoryDepartureList[0].time.hour.toString().padStart(2, '0'), weekendsDormitoryDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForDormitoryLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsDormitoryDepartureList[weekendsDormitoryDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekendsDormitoryDepartureList[weekendsDormitoryDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekdaysTerminalDepartureList.isNotEmpty()) {
                            shuttleForTerminalFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysTerminalDepartureList[0].time.hour.toString().padStart(2, '0'), weekdaysTerminalDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForTerminalLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysTerminalDepartureList[weekdaysTerminalDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekdaysTerminalDepartureList[weekdaysTerminalDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekendsTerminalDepartureList.isNotEmpty()) {
                            shuttleForTerminalFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsTerminalDepartureList[0].time.hour.toString().padStart(2, '0'), weekendsTerminalDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForTerminalLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsTerminalDepartureList[weekendsTerminalDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekendsTerminalDepartureList[weekendsTerminalDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekdaysJungangStationDepartureList.isNotEmpty()) {
                            shuttleForJungangStationFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysJungangStationDepartureList[0].time.hour.toString().padStart(2, '0'), weekdaysJungangStationDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForJungangStationLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysJungangStationDepartureList[weekdaysJungangStationDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekdaysJungangStationDepartureList[weekdaysJungangStationDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekendsJungangStationDepartureList.isNotEmpty()) {
                            shuttleForJungangStationFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsJungangStationDepartureList[0].time.hour.toString().padStart(2, '0'), weekendsJungangStationDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForJungangStationLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsJungangStationDepartureList[weekendsJungangStationDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekendsJungangStationDepartureList[weekendsJungangStationDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                    }
                }
                else -> {
                    val departureList = it.first().entries.sortedBy { item -> item.time }
                    val weekdaysDepartureList = departureList.filter { item -> item.weekday }
                    val weekendsDepartureList = departureList.filter { item -> !item.weekday }
                    binding.apply {
                        if (weekdaysDepartureList.isNotEmpty()) {
                            shuttleForDormitoryFirstTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysDepartureList[0].time.hour.toString().padStart(2, '0'), weekdaysDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForDormitoryLastTimeWeekdays.text = getString(R.string.shuttle_first_last_time_format_weekdays, weekdaysDepartureList[weekdaysDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekdaysDepartureList[weekdaysDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
                        }
                        if (weekendsDepartureList.isNotEmpty()) {
                            shuttleForDormitoryFirstTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsDepartureList[0].time.hour.toString().padStart(2, '0'), weekendsDepartureList[0].time.minute.toString().padStart(2, '0'))
                            shuttleForDormitoryLastTimeWeekends.text = getString(R.string.shuttle_first_last_time_format_weekends, weekendsDepartureList[weekendsDepartureList.size - 1].time.hour.toString().padStart(2, '0'), weekendsDepartureList[weekendsDepartureList.size - 1].time.minute.toString().padStart(2, '0'))
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
