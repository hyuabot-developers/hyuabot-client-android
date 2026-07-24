package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import app.kobuggi.hyuabot.databinding.DialogShuttleQuickSettingsBinding
import app.kobuggi.hyuabot.ui.home.HomeSubwayTransferDestination
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
        val showByDestination = requireArguments().getBoolean(ARG_SHOW_BY_DESTINATION)
        val showDepartureTime = requireArguments().getBoolean(ARG_SHOW_DEPARTURE_TIME)
        val showPresenceStatus = requireArguments().getBoolean(ARG_SHOW_PRESENCE_STATUS, true)
        val showBusTransfer = requireArguments().getBoolean(ARG_SHOW_BUS_TRANSFER, true)
        val showSubwayTransfer = requireArguments().getBoolean(ARG_SHOW_SUBWAY_TRANSFER, true)
        var subwayDestination = HomeSubwayTransferDestination.from(
            requireArguments().getString(ARG_SUBWAY_DESTINATION),
        )
        var alternativeMode = ShuttleAlternativeDisplayMode.from(
            requireArguments().getString(ARG_ALTERNATIVE_MODE),
        )
        binding.showByDestinationSwitch.isChecked = showByDestination
        binding.showDepartureTimeSwitch.isChecked = showDepartureTime
        binding.showPresenceStatusSwitch.isChecked = showPresenceStatus
        binding.showBusTransferSwitch.isChecked = showBusTransfer
        binding.showSubwayTransferSwitch.isChecked = showSubwayTransfer
        binding.subwayDestinationButton.isEnabled = showSubwayTransfer
        binding.subwayDestinationButton.alpha = if (showSubwayTransfer) 1f else DISABLED_ALPHA
        binding.subwayDestinationButton.setText(subwayDestination.titleRes)
        binding.alternativeModeButton.setText(alternativeMode.titleRes)

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
        binding.showPresenceStatusSwitch.setOnCheckedChangeListener { _, isChecked ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putBoolean(KEY_SHOW_PRESENCE_STATUS, isChecked)
                },
            )
        }
        binding.showBusTransferSwitch.setOnCheckedChangeListener { _, isChecked ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putBoolean(KEY_SHOW_BUS_TRANSFER, isChecked)
                },
            )
        }
        binding.showSubwayTransferSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.subwayDestinationButton.isEnabled = isChecked
            binding.subwayDestinationButton.alpha = if (isChecked) 1f else DISABLED_ALPHA
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putBoolean(KEY_SHOW_SUBWAY_TRANSFER, isChecked)
                },
            )
        }
        binding.subwayDestinationButton.setOnClickListener { anchor ->
            PopupMenu(requireContext(), anchor).apply {
                HomeSubwayTransferDestination.entries.forEachIndexed { index, destination ->
                    menu.add(0, index, index, destination.titleRes)
                }
                setOnMenuItemClickListener { item ->
                    subwayDestination = HomeSubwayTransferDestination.entries[item.itemId]
                    binding.subwayDestinationButton.setText(subwayDestination.titleRes)
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        Bundle().apply {
                            putString(KEY_SUBWAY_DESTINATION, subwayDestination.value)
                        },
                    )
                    true
                }
                show()
            }
        }
        binding.alternativeModeButton.setOnClickListener { anchor ->
            PopupMenu(requireContext(), anchor).apply {
                ShuttleAlternativeDisplayMode.entries.forEachIndexed { index, mode ->
                    menu.add(0, index, index, mode.titleRes)
                }
                setOnMenuItemClickListener { item ->
                    alternativeMode = ShuttleAlternativeDisplayMode.entries[item.itemId]
                    binding.alternativeModeButton.setText(alternativeMode.titleRes)
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        Bundle().apply {
                            putString(KEY_ALTERNATIVE_MODE, alternativeMode.value)
                        },
                    )
                    true
                }
                show()
            }
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
        const val KEY_SHOW_PRESENCE_STATUS = "show_presence_status"
        const val KEY_SHOW_BUS_TRANSFER = "show_bus_transfer"
        const val KEY_SHOW_SUBWAY_TRANSFER = "show_subway_transfer"
        const val KEY_SUBWAY_DESTINATION = "subway_destination"
        const val KEY_ALTERNATIVE_MODE = "alternative_mode"
        const val KEY_OPEN_HOME = "open_home"
        private const val ARG_SHOW_BY_DESTINATION = "arg_show_by_destination"
        private const val ARG_SHOW_DEPARTURE_TIME = "arg_show_departure_time"
        private const val ARG_SHOW_PRESENCE_STATUS = "arg_show_presence_status"
        private const val ARG_SHOW_BUS_TRANSFER = "arg_show_bus_transfer"
        private const val ARG_SHOW_SUBWAY_TRANSFER = "arg_show_subway_transfer"
        private const val ARG_SUBWAY_DESTINATION = "arg_subway_destination"
        private const val ARG_ALTERNATIVE_MODE = "arg_alternative_mode"
        private const val DISABLED_ALPHA = 0.38f

        fun newInstance(
            showByDestination: Boolean,
            showDepartureTime: Boolean,
            showPresenceStatus: Boolean,
            showBusTransfer: Boolean,
            showSubwayTransfer: Boolean,
            subwayDestination: HomeSubwayTransferDestination,
            alternativeMode: ShuttleAlternativeDisplayMode,
        ): ShuttleQuickSettingsDialog {
            return ShuttleQuickSettingsDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_BY_DESTINATION, showByDestination)
                    putBoolean(ARG_SHOW_DEPARTURE_TIME, showDepartureTime)
                    putBoolean(ARG_SHOW_PRESENCE_STATUS, showPresenceStatus)
                    putBoolean(ARG_SHOW_BUS_TRANSFER, showBusTransfer)
                    putBoolean(ARG_SHOW_SUBWAY_TRANSFER, showSubwayTransfer)
                    putString(ARG_SUBWAY_DESTINATION, subwayDestination.value)
                    putString(ARG_ALTERNATIVE_MODE, alternativeMode.value)
                }
            }
        }
    }
}
