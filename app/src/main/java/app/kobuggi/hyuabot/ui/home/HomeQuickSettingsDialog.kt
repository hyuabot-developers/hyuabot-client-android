package app.kobuggi.hyuabot.ui.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import app.kobuggi.hyuabot.databinding.DialogHomeQuickSettingsBinding
import app.kobuggi.hyuabot.ui.bus.realtime.BusSeoulTargetStop
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HomeQuickSettingsDialog : BottomSheetDialogFragment() {
    private var _binding: DialogHomeQuickSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogHomeQuickSettingsBinding.inflate(inflater, container, false)
        binding.subwayDestinationSeoul.tag = HomeSubwayTransferDestination.SEOUL
        binding.subwayDestinationSuwonYongin.tag = HomeSubwayTransferDestination.SUWON_YONGIN
        binding.subwayDestinationIncheon.tag = HomeSubwayTransferDestination.INCHEON
        binding.subwayDestinationOido.tag = HomeSubwayTransferDestination.OIDO
        binding.subwayDestinationSosa.tag = HomeSubwayTransferDestination.SOSA
        binding.seoulBusStopSeocho.tag = BusSeoulTargetStop.SEOCHO
        binding.seoulBusStopGyodae.tag = BusSeoulTargetStop.GYODAE
        binding.seoulBusStopGangnam.tag = BusSeoulTargetStop.GANGNAM
        binding.seoulBusStopYangjae.tag = BusSeoulTargetStop.YANGJAE
        binding.seoulBusStopYangjaeForest.tag = BusSeoulTargetStop.YANGJAE_CITIZENS_FOREST
        binding.showPresenceStatusSwitch.isChecked = requireArguments().getBoolean(ARG_SHOW_PRESENCE_STATUS, true)
        binding.showBus50TransferSwitch.isChecked = requireArguments().getBoolean(ARG_SHOW_BUS50_TRANSFER, true)
        binding.showSubwayTransferSwitch.isChecked = requireArguments().getBoolean(ARG_SHOW_SUBWAY_TRANSFER, true)
        val subwayDestination = HomeSubwayTransferDestination.from(requireArguments().getString(ARG_SUBWAY_TRANSFER_DESTINATION))
        binding.subwayDestinationGroup.check(buttonIdFor(subwayDestination))
        binding.showSeoulBusStopSwitch.isChecked = requireArguments().getBoolean(ARG_SHOW_SEOUL_BUS_STOP, true)
        val seoulBusStop = BusSeoulTargetStop.from(requireArguments().getString(ARG_SEOUL_BUS_STOP))
        binding.seoulBusStopGroup.check(buttonIdFor(seoulBusStop))
        updateSeoulBusStopEnabled(binding.showSeoulBusStopSwitch.isChecked)
        reorderHomeSettingsSections()
        updateSubwayDestinationEnabled(binding.showSubwayTransferSwitch.isChecked)
        return binding.root
    }

    private fun reorderHomeSettingsSections() {
        val content = binding.root.getChildAt(0) as? ViewGroup ?: return
        val sections = listOf(
            binding.presenceStatusRow,
            binding.subwayTransferRow,
            binding.bus50TransferRow,
            binding.seoulBusStopRow,
        )
        sections.forEach(content::removeView)
        sections.forEachIndexed { index, section ->
            content.addView(section, index + 2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.showPresenceStatusSwitch.setOnCheckedChangeListener { _, isChecked ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putBoolean(KEY_SHOW_PRESENCE_STATUS, isChecked) },
            )
        }
        binding.showBus50TransferSwitch.setOnCheckedChangeListener { _, isChecked ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putBoolean(KEY_SHOW_BUS50_TRANSFER, isChecked) },
            )
        }
        binding.showSubwayTransferSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateSubwayDestinationEnabled(isChecked)
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putBoolean(KEY_SHOW_SUBWAY_TRANSFER, isChecked) },
            )
        }
        binding.subwayDestinationGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val destination = group.findViewById<View>(checkedId)?.tag as? HomeSubwayTransferDestination ?: return@addOnButtonCheckedListener
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putString(KEY_SUBWAY_TRANSFER_DESTINATION, destination.value) },
            )
        }
        binding.showSeoulBusStopSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateSeoulBusStopEnabled(isChecked)
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putBoolean(KEY_SHOW_SEOUL_BUS_STOP, isChecked) },
            )
        }
        binding.seoulBusStopGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val stop = group.findViewById<View>(checkedId)?.tag as? BusSeoulTargetStop
                ?: return@addOnButtonCheckedListener
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putString(KEY_SEOUL_BUS_STOP, stop.value) },
            )
        }
        binding.legacyShuttleRow.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putBoolean(KEY_OPEN_LEGACY_SHUTTLE, true) },
            )
            dismiss()
        }
        binding.inquiryRow.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putBoolean(KEY_OPEN_INQUIRY, true) },
            )
            dismiss()
        }
    }

    private fun buttonIdFor(destination: HomeSubwayTransferDestination): Int {
        val button = binding.subwayDestinationGroup.findViewWithTag<MaterialButton>(destination)
        return button?.id ?: binding.subwayDestinationSeoul.id
    }

    private fun buttonIdFor(stop: BusSeoulTargetStop): Int {
        val button = binding.seoulBusStopGroup.findViewWithTag<MaterialButton>(stop)
        return button?.id ?: binding.seoulBusStopGangnam.id
    }

    private fun updateSubwayDestinationEnabled(isEnabled: Boolean) {
        binding.subwayDestinationGroup.isEnabled = isEnabled
        binding.subwayDestinationGroup.alpha = if (isEnabled) 1f else 0.45f
        for (index in 0 until binding.subwayDestinationGroup.childCount) {
            binding.subwayDestinationGroup.getChildAt(index).isEnabled = isEnabled
        }
    }

    private fun updateSeoulBusStopEnabled(isEnabled: Boolean) {
        binding.seoulBusStopGroup.isEnabled = isEnabled
        binding.seoulBusStopGroup.alpha = if (isEnabled) 1f else 0.45f
        for (index in 0 until binding.seoulBusStopGroup.childCount) {
            binding.seoulBusStopGroup.getChildAt(index).isEnabled = isEnabled
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "HomeQuickSettingsDialog"
        const val KEY_OPEN_LEGACY_SHUTTLE = "openLegacyShuttle"
        const val KEY_OPEN_INQUIRY = "openInquiry"
        const val KEY_SHOW_PRESENCE_STATUS = "showPresenceStatus"
        const val KEY_SHOW_BUS50_TRANSFER = "showBus50Transfer"
        const val KEY_SHOW_SUBWAY_TRANSFER = "showSubwayTransfer"
        const val KEY_SUBWAY_TRANSFER_DESTINATION = "subwayTransferDestination"
        const val KEY_SHOW_SEOUL_BUS_STOP = "showSeoulBusStop"
        const val KEY_SEOUL_BUS_STOP = "seoulBusStop"
        private const val ARG_SHOW_PRESENCE_STATUS = "showPresenceStatus"
        private const val ARG_SHOW_BUS50_TRANSFER = "showBus50Transfer"
        private const val ARG_SHOW_SUBWAY_TRANSFER = "showSubwayTransfer"
        private const val ARG_SUBWAY_TRANSFER_DESTINATION = "subwayTransferDestination"
        private const val ARG_SHOW_SEOUL_BUS_STOP = "showSeoulBusStop"
        private const val ARG_SEOUL_BUS_STOP = "seoulBusStop"

        fun newInstance(
            showPresenceStatus: Boolean,
            showBus50Transfer: Boolean,
            showSubwayTransfer: Boolean,
            subwayTransferDestination: HomeSubwayTransferDestination,
            showSeoulBusStop: Boolean,
            seoulBusStop: BusSeoulTargetStop,
        ): HomeQuickSettingsDialog {
            return HomeQuickSettingsDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_PRESENCE_STATUS, showPresenceStatus)
                    putBoolean(ARG_SHOW_BUS50_TRANSFER, showBus50Transfer)
                    putBoolean(ARG_SHOW_SUBWAY_TRANSFER, showSubwayTransfer)
                    putString(ARG_SUBWAY_TRANSFER_DESTINATION, subwayTransferDestination.value)
                    putBoolean(ARG_SHOW_SEOUL_BUS_STOP, showSeoulBusStop)
                    putString(ARG_SEOUL_BUS_STOP, seoulBusStop.value)
                }
            }
        }
    }
}
