package app.kobuggi.hyuabot.ui.setting
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import app.kobuggi.hyuabot.R
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.DialogSettingCampusBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CampusSettingDialog @Inject constructor() : DialogFragment() {
    private val binding by lazy { DialogSettingCampusBinding.inflate(layoutInflater) }
    private val vm by viewModels<CampusSettingDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {
            vm.campusID.observe(viewLifecycleOwner) { campusID ->
                markSelected(campusErica, campusID == 2)
                markSelected(campusSeoul, campusID == 1)
            }
            campusSeoul.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_CAMPUS, type = AnalyticsContentType.MENU, name = "seoul")
                vm.setCampusID(1)
                dismiss()
            }
            campusErica.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_CAMPUS, type = AnalyticsContentType.MENU, name = "erica")
                vm.setCampusID(2)
                dismiss()
            }
        }
        return binding.root
    }

    private fun markSelected(view: TextView, isSelected: Boolean) {
        view.setBackgroundColor(
            ContextCompat.getColor(requireContext(), if (isSelected) R.color.app_selection_background else android.R.color.transparent)
        )
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (isSelected) R.drawable.ic_check else 0, 0)
        view.compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.setting_selection_check_padding)
    }
}
