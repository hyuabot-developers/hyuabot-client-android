package app.kobuggi.hyuabot.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {
            settingCampus.setOnClickListener { openCampusDialog() }
            settingLanguage.setOnClickListener { openLanguageDialog() }
            settingTheme.setOnClickListener { openThemeDialog() }
        }
        return binding.root
    }

    private fun openThemeDialog() {
        SettingFragmentDirections.actionSettingFragmentToThemeSettingDialogFragment().also {
            findNavController().navigate(it)
        }
    }

    private fun openCampusDialog() {
        SettingFragmentDirections.actionSettingFragmentToCampusSettingDialogFragment().also {
            findNavController().navigate(it)
        }
    }

    private fun openLanguageDialog() {
        SettingFragmentDirections.actionSettingFragmentToLanguageSettingDialogFragment().also {
            findNavController().navigate(it)
        }
    }
}
