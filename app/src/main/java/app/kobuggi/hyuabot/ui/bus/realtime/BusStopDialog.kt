package app.kobuggi.hyuabot.ui.bus.realtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogBusStopBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BusStopDialog @Inject constructor() : DialogFragment() {
    private val binding by lazy { DialogBusStopBinding.inflate(layoutInflater) }
    private val viewModel: BusStopDialogViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.selectedStopID.observe(viewLifecycleOwner) {
            when (it) {
                R.string.bus_stop_convention -> binding.busStopDropdown.setText(getString(R.string.bus_stop_convention), false)
                R.string.bus_stop_dormitory -> binding.busStopDropdown.setText(getString(R.string.bus_stop_dormitory), false)
                R.string.bus_stop_cluster -> binding.busStopDropdown.setText(getString(R.string.bus_stop_cluster), false)
            }
        }
        binding.toolbar.setOnMenuItemClickListener { _ -> dismiss(); true }
        binding.busStopDropdown.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> viewModel.selectedStopID.value = R.string.bus_stop_dormitory
                1 -> viewModel.selectedStopID.value = R.string.bus_stop_cluster
                2 -> viewModel.selectedStopID.value = R.string.bus_stop_convention
            }
        }
        binding.confirmButton.setOnClickListener {
            if (viewModel.selectedStopID.value != null) {
                viewModel.setBusStop(viewModel.selectedStopID.value!!)
                dismiss()
            }
            dismiss()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
