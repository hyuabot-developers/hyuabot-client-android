package app.kobuggi.hyuabot.ui.bus.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogBusRouteBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BusRouteDialog @Inject constructor() : DialogFragment() {
    private val binding by lazy { DialogBusRouteBinding.inflate(layoutInflater) }
    private val viewModel: BusRouteDialogViewModel by viewModels()
    private val args: BusRouteDialogArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.fetchData(args.stopID, args.routeID)
        viewModel.busRoute.observe(viewLifecycleOwner) {
            binding.apply {
                busRouteName.text = getString(R.string.bus_info_route, it.info.name)
                busRouteStartStop.text = getString(R.string.bus_info_start_stop, it.info.start.name)
                busRouteTimeFromStartStop.text = getString(R.string.bus_info_time_from_start, it.minuteFromStart)
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
