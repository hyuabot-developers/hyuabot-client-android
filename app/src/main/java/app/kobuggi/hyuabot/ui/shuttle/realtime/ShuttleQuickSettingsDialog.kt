package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.kobuggi.hyuabot.databinding.DialogShuttleQuickSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShuttleQuickSettingsDialog : BottomSheetDialogFragment() {
    private var _binding: DialogShuttleQuickSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogShuttleQuickSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.showByDestinationSwitch.isChecked = requireArguments().getBoolean(ARG_SHOW_BY_DESTINATION)
        binding.showDepartureTimeSwitch.isChecked = requireArguments().getBoolean(ARG_SHOW_DEPARTURE_TIME)

        binding.showByDestinationRow.setOnClickListener {
            binding.showByDestinationSwitch.toggle()
        }
        binding.showDepartureTimeRow.setOnClickListener {
            binding.showDepartureTimeSwitch.toggle()
        }
        binding.showByDestinationSwitch.setOnCheckedChangeListener { _, isChecked ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putBoolean(KEY_SHOW_BY_DESTINATION, isChecked)
                },
            )
        }
        binding.showDepartureTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putBoolean(KEY_SHOW_DEPARTURE_TIME, isChecked)
                },
            )
        }
        binding.openHomeButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putBoolean(KEY_OPEN_HOME, true)
                },
            )
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "shuttle_quick_settings"
        const val KEY_SHOW_BY_DESTINATION = "show_by_destination"
        const val KEY_SHOW_DEPARTURE_TIME = "show_departure_time"
        const val KEY_OPEN_HOME = "open_home"
        private const val ARG_SHOW_BY_DESTINATION = "arg_show_by_destination"
        private const val ARG_SHOW_DEPARTURE_TIME = "arg_show_departure_time"

        fun newInstance(showByDestination: Boolean, showDepartureTime: Boolean): ShuttleQuickSettingsDialog {
            return ShuttleQuickSettingsDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_BY_DESTINATION, showByDestination)
                    putBoolean(ARG_SHOW_DEPARTURE_TIME, showDepartureTime)
                }
            }
        }
    }
}
