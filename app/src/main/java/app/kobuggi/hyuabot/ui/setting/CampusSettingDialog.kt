package app.kobuggi.hyuabot.ui.setting
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.kobuggi.hyuabot.R
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.DialogSettingCampusBinding
import app.kobuggi.hyuabot.widget.refreshHyuabotWidgets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CampusSettingDialog @Inject constructor() : SettingChoiceDialogFragment() {
    private val binding by lazy { DialogSettingCampusBinding.inflate(layoutInflater) }
    private val vm by viewModels<CampusSettingDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {
            campusSeoul.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_CAMPUS, type = AnalyticsContentType.MENU, name = "seoul")
                viewLifecycleOwner.lifecycleScope.launch {
                    vm.setCampusID(1)
                    refreshHyuabotWidgets(requireContext())
                    dismiss()
                }
            }
            campusErica.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_CAMPUS, type = AnalyticsContentType.MENU, name = "erica")
                viewLifecycleOwner.lifecycleScope.launch {
                    vm.setCampusID(2)
                    refreshHyuabotWidgets(requireContext())
                    dismiss()
                }
            }
        }
        return binding.root
    }
}
