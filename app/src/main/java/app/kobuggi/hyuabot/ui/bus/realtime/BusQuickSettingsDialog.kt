package app.kobuggi.hyuabot.ui.bus.realtime

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import app.kobuggi.hyuabot.databinding.DialogBusQuickSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BusQuickSettingsDialog : BottomSheetDialogFragment() {
    private var _binding: DialogBusQuickSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogBusQuickSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val showSecondaryEta = requireArguments().getBoolean(ARG_SHOW_SECONDARY_ETA, true)
        var seoulTarget = BusSeoulTargetStop.from(requireArguments().getString(ARG_SEOUL_TARGET))

        binding.showSecondaryEtaSwitch.isChecked = showSecondaryEta
        binding.seoulTargetButton.setText(seoulTarget.titleRes)
        binding.seoulTargetButton.isEnabled = showSecondaryEta
        binding.seoulTargetButton.alpha = if (showSecondaryEta) 1f else DISABLED_ALPHA

        binding.showSecondaryEtaSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.seoulTargetButton.isEnabled = isChecked
            binding.seoulTargetButton.alpha = if (isChecked) 1f else DISABLED_ALPHA
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putBoolean(KEY_SHOW_SECONDARY_ETA, isChecked)
                },
            )
        }
        binding.seoulTargetButton.setOnClickListener { anchor ->
            PopupMenu(requireContext(), anchor).apply {
                BusSeoulTargetStop.entries.forEachIndexed { index, destination ->
                    menu.add(0, index, index, destination.titleRes)
                }
                setOnMenuItemClickListener { item ->
                    seoulTarget = BusSeoulTargetStop.entries[item.itemId]
                    binding.seoulTargetButton.setText(seoulTarget.titleRes)
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        Bundle().apply {
                            putString(KEY_SEOUL_TARGET, seoulTarget.value)
                        },
                    )
                    true
                }
                show()
            }
        }
        binding.openHelpButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putBoolean(KEY_OPEN_HELP, true) },
            )
            dismiss()
        }
        binding.openInquiryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putBoolean(KEY_OPEN_INQUIRY, true) },
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
        const val REQUEST_KEY = "bus_quick_settings"
        const val KEY_SHOW_SECONDARY_ETA = "show_secondary_eta"
        const val KEY_SEOUL_TARGET = "seoul_target"
        const val KEY_OPEN_HELP = "open_help"
        const val KEY_OPEN_INQUIRY = "open_inquiry"
        private const val ARG_SHOW_SECONDARY_ETA = "arg_show_secondary_eta"
        private const val ARG_SEOUL_TARGET = "arg_seoul_target"
        private const val DISABLED_ALPHA = 0.38f

        fun newInstance(
            showSecondaryEta: Boolean,
            seoulTarget: BusSeoulTargetStop,
        ): BusQuickSettingsDialog {
            return BusQuickSettingsDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_SECONDARY_ETA, showSecondaryEta)
                    putString(ARG_SEOUL_TARGET, seoulTarget.value)
                }
            }
        }
    }
}
