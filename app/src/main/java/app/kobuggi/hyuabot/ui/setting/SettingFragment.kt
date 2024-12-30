package app.kobuggi.hyuabot.ui.setting

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.databinding.FragmentSettingBinding
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment @Inject constructor() : Fragment(), DialogInterface.OnDismissListener {
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
            appInfo.setOnClickListener { openInfoDialog() }
        }
        return binding.root
    }

    private fun openThemeDialog() {
        SettingFragmentDirections.actionSettingFragmentToThemeSettingDialogFragment().also {
            findNavController().safeNavigate(it)
        }
    }

    private fun openCampusDialog() {
        SettingFragmentDirections.actionSettingFragmentToCampusSettingDialogFragment().also {
            findNavController().safeNavigate(it)
        }
    }

    private fun openLanguageDialog() {
        SettingFragmentDirections.actionSettingFragmentToLanguageSettingDialogFragment().also {
            findNavController().safeNavigate(it)
        }
    }

    private fun openInfoDialog() {
        SettingFragmentDirections.actionSettingFragmentToSettingDeveloperDialogFragment().also {
            findNavController().safeNavigate(it)
        }
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        if (requireActivity() is MainActivity){
            requireActivity().runOnUiThread {
                (requireActivity() as MainActivity).onDismiss(dialogInterface)
            }
        }
    }
}
