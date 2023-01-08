package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogShuttleStopInformationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShuttleStopInformationDialog : DialogFragment(), OnMapReadyCallback {
    private lateinit var binding: DialogShuttleStopInformationBinding
    private val viewModel: ShuttleStopInformationViewModel by viewModels()
    private val parentViewModel: RealtimeViewModel by viewModels({requireParentFragment()})
    private var location: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Dialog)
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST){

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogShuttleStopInformationBinding.inflate(inflater, container, false)
        parentViewModel.shuttleStopInformationEvent.observe(viewLifecycleOwner) { it ->
            when(it) {
                0 -> {
                    binding.shuttleStopName.text = getString(R.string.dormitory_o)
                    binding.shuttleRunningTimeTableStationWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableStationWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableTerminalWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableTerminalWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableJungangStnWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableJungangStnWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableCampusWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableCampusWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableDormitoryWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableDormitoryWeekends.visibility = View.GONE
                    location = LatLng(37.29339607529377, 126.83630604103446)
                    viewModel.fetchTimetable("dormitory_o")
                    viewModel.shuttleToStationWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeStationWeekdays.text = timetable.first()
                                binding.shuttleLastTimeStationWeekdays.text = timetable.last()
                            }
                        }

                    }
                    viewModel.shuttleToStationWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeStationWeekends.text = timetable.first()
                                binding.shuttleLastTimeStationWeekends.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToTerminalWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeTerminalWeekdays.text = timetable.first()
                                binding.shuttleLastTimeTerminalWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToTerminalWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeTerminalWeekends.text = timetable.first()
                                binding.shuttleLastTimeTerminalWeekends.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToJungangStnWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeJungangStnWeekdays.text = timetable.first()
                                binding.shuttleLastTimeJungangStnWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToJungangStnWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeJungangStnWeekends.text = timetable.first()
                                binding.shuttleLastTimeJungangStnWeekends.text = timetable.last()
                            }
                        }
                    }
                }
                1 -> {
                    binding.shuttleStopName.text = getString(R.string.shuttlecock_o)
                    binding.shuttleRunningTimeTableStationWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableStationWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableTerminalWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableTerminalWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableJungangStnWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableJungangStnWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableCampusWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableCampusWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableDormitoryWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableDormitoryWeekends.visibility = View.GONE
                    location = LatLng(37.29875417910844, 126.83784054072336)
                    viewModel.fetchTimetable("shuttlecock_o")
                    viewModel.shuttleToStationWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeStationWeekdays.text = timetable.first()
                                binding.shuttleLastTimeStationWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToStationWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeStationWeekends.text = timetable.first()
                                binding.shuttleLastTimeStationWeekends.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToTerminalWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeTerminalWeekdays.text = timetable.first()
                                binding.shuttleLastTimeTerminalWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToTerminalWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeTerminalWeekends.text = timetable.first()
                                binding.shuttleLastTimeTerminalWeekends.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToJungangStnWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeJungangStnWeekdays.text = timetable.first()
                                binding.shuttleLastTimeJungangStnWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToJungangStnWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeJungangStnWeekends.text = timetable.first()
                                binding.shuttleLastTimeJungangStnWeekends.text = timetable.last()
                            }
                        }
                    }
                }
                2 -> {
                    binding.shuttleStopName.text = getString(R.string.station)
                    binding.shuttleRunningTimeTableStationWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableStationWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableTerminalWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableTerminalWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableJungangStnWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableJungangStnWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableCampusWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableCampusWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableDormitoryWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableDormitoryWeekends.visibility = View.GONE
                    location = LatLng(37.308494476826155, 126.85310236423418)
                    viewModel.fetchTimetable("station")
                    viewModel.shuttleToCampusWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeCampusWeekdays.text = timetable.first()
                                binding.shuttleLastTimeCampusWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToCampusWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeCampusWeekends.text = timetable.first()
                                binding.shuttleLastTimeCampusWeekends.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToTerminalWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeTerminalWeekdays.text = timetable.first()
                                binding.shuttleLastTimeTerminalWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToTerminalWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeTerminalWeekends.text = timetable.first()
                                binding.shuttleLastTimeTerminalWeekends.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToJungangStnWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeJungangStnWeekdays.text = timetable.first()
                                binding.shuttleLastTimeJungangStnWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToJungangStnWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeJungangStnWeekends.text = timetable.first()
                                binding.shuttleLastTimeJungangStnWeekends.text = timetable.last()
                            }
                        }
                    }
                }
                3 -> {
                    binding.shuttleStopName.text = getString(R.string.terminal)
                    binding.shuttleRunningTimeTableStationWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableStationWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableTerminalWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableTerminalWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableJungangStnWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableJungangStnWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableCampusWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableCampusWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableDormitoryWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableDormitoryWeekends.visibility = View.GONE
                    location = LatLng(37.31945164682341, 126.8455453372041)
                    viewModel.fetchTimetable("terminal")
                    viewModel.shuttleToCampusWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeCampusWeekdays.text = timetable.first()
                                binding.shuttleLastTimeCampusWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToCampusWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeCampusWeekends.text = timetable.first()
                                binding.shuttleLastTimeCampusWeekends.text = timetable.last()
                            }
                        }
                    }
                }
                4 -> {
                    binding.shuttleStopName.text = getString(R.string.jungang_stn)
                    binding.shuttleRunningTimeTableStationWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableStationWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableTerminalWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableTerminalWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableJungangStnWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableJungangStnWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableCampusWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableCampusWeekends.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableDormitoryWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableDormitoryWeekends.visibility = View.GONE
                    location = LatLng(37.31488837705007, 126.83973408036947)
                    viewModel.fetchTimetable("jungang_stn")
                    viewModel.shuttleToCampusWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeCampusWeekdays.text = timetable.first()
                                binding.shuttleLastTimeCampusWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToCampusWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeCampusWeekends.text = timetable.first()
                                binding.shuttleLastTimeCampusWeekends.text = timetable.last()
                            }
                        }
                    }
                }
                5 -> {
                    binding.shuttleStopName.text = getString(R.string.shuttlecock_i)
                    binding.shuttleRunningTimeTableStationWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableStationWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableTerminalWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableTerminalWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableJungangStnWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableJungangStnWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableCampusWeekdays.visibility = View.GONE
                    binding.shuttleRunningTimeTableCampusWeekends.visibility = View.GONE
                    binding.shuttleRunningTimeTableDormitoryWeekdays.visibility = View.VISIBLE
                    binding.shuttleRunningTimeTableDormitoryWeekends.visibility = View.VISIBLE
                    location = LatLng(37.29869328231496, 126.8377767466817)
                    viewModel.fetchTimetable("shuttlecock_i")
                    viewModel.shuttleToDormitoryWeekdays.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeDormitoryWeekdays.text = timetable.first()
                                binding.shuttleLastTimeDormitoryWeekdays.text = timetable.last()
                            }
                        }
                    }
                    viewModel.shuttleToDormitoryWeekends.observe(viewLifecycleOwner) {
                        timetable -> run {
                            if (timetable.isNotEmpty()) {
                                binding.shuttleFirstTimeDormitoryWeekends.text = timetable.first()
                                binding.shuttleLastTimeDormitoryWeekends.text = timetable.last()
                            }
                        }
                    }
                }
            }
        }
        binding.dialogCloseButton.setOnClickListener {
            dismiss()
        }
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val parentFragment = parentFragment
        if (parentFragment is RealtimeFragment) {
            parentFragment.onDismiss(dialog)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        try {
            val applyStyleSuccess = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map
                )
            )
            if (!applyStyleSuccess) {
                Log.e("Map", "Style parsing failed.")
            }
        } catch (e: Exception) {
            Log.e("Map", "Can't find style. Error: ", e)
        }
        if (location != null) {
            val markerOptions = MarkerOptions()
                .position(location!!)
                .title(binding.shuttleStopName.text.toString())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop))
            val marker = map.addMarker(markerOptions)
            marker?.showInfoWindow()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location!!, 16f))
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.2964424247715, 126.83516599434644), 16f))
        }
    }
}