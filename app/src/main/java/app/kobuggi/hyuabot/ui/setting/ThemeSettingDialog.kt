package app.kobuggi.hyuabot.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
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
            themeSystem.setOnClickListener {
                vm.setDarkModeSystem()
                dismiss()
            }
            themeLight.setOnClickListener {
                vm.setDarkMode(false)
                dismiss()
            }
            themeDark.setOnClickListener {
                vm.setDarkMode(true)
                dismiss()
            }
        }

        return binding.root
    }
}
