package app.kobuggi.hyuabot.ui.setting

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.databinding.FragmentSettingBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.MainActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment @Inject constructor() : Fragment(), DialogInterface.OnDismissListener {
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var updatingSwitch = false
        binding.settingAnalyticsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!updatingSwitch) {
                viewLifecycleOwner.lifecycleScope.launch {
                    userPreferencesRepository.setAnalyticsConsent(isChecked)
                    FirebaseAnalytics.getInstance(requireContext()).setAnalyticsCollectionEnabled(isChecked)
                    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(isChecked)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userPreferencesRepository.analyticsConsent.collect { enabled ->
                    updatingSwitch = true
                    binding.settingAnalyticsSwitch.isChecked = enabled
                    updatingSwitch = false
                }
            }
        }
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
