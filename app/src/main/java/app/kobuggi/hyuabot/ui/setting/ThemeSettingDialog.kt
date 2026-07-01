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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogSettingThemeBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ThemeSettingDialog : DialogFragment(){
    private lateinit var binding: DialogSettingThemeBinding
    private val vm by viewModels<ThemeSettingDialogViewModel>()
    @Inject lateinit var dataStore: DataStore<Preferences>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogSettingThemeBinding.inflate(inflater, container, false)
        binding.apply {
            vm.theme.observe(viewLifecycleOwner) { theme ->
                markSelected(themeSystem, theme == null)
                markSelected(themeLight, theme == "light")
                markSelected(themeDark, theme == "dark")
            }
            themeSystem.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_THEME, type = AnalyticsContentType.MENU, name = "system")
                vm.setDarkModeSystem()
                dismiss()
            }
            themeLight.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_THEME, type = AnalyticsContentType.MENU, name = "light")
                vm.setDarkMode(false)
                dismiss()
            }
            themeDark.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_THEME, type = AnalyticsContentType.MENU, name = "dark")
                vm.setDarkMode(true)
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
