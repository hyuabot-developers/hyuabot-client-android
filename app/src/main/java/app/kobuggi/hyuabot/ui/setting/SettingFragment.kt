package app.kobuggi.hyuabot.ui.setting
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.databinding.FragmentSettingBinding
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.safeNavigate
import app.kobuggi.hyuabot.ui.MainActivity
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.coachmark.CoachmarkStep
import app.kobuggi.hyuabot.ui.common.coachmark.showCoachmarkOnce
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
            settingCampus.setOnClickListener { AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM, name = "campus"); openCampusDialog() }
            settingLanguage.setOnClickListener { AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM, name = "language"); openLanguageDialog() }
            settingTheme.setOnClickListener { AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM, name = "theme"); openThemeDialog() }
            appInfo.setOnClickListener { AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM, name = "info"); openInfoDialog() }
            settingCoachmarkReset.setOnClickListener {
                lifecycleScope.launch {
                    Coachmarks.USER_VISIBLE_KEYS.forEach { userPreferencesRepository.resetCoachmark(it) }
                    Toast.makeText(requireContext(), R.string.coachmark_reset_complete, Toast.LENGTH_SHORT).show()
                }
            }
            appInfo.setOnLongClickListener {
                lifecycleScope.launch {
                    userPreferencesRepository.resetCoachmark(Coachmarks.SHUTTLE_REALTIME_UPDATES)
                    Toast.makeText(requireContext(), R.string.coachmark_shuttle_realtime_updates_reset, Toast.LENGTH_SHORT).show()
                }
                true
            }
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
                    FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = isChecked
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userPreferencesRepository.analyticsConsent.collect { enabled ->
                        updatingSwitch = true
                        binding.settingAnalyticsSwitch.isChecked = enabled
                        updatingSwitch = false
                    }
                }
                launch {
                    userPreferencesRepository.campusID.collect { campusID ->
                        binding.settingCampusValue.text = getString(
                            if (campusID == 1) R.string.campus_seoul else R.string.campus_erica
                        )
                    }
                }
                launch {
                    userPreferencesRepository.theme.collect { theme ->
                        binding.settingThemeValue.text = getString(
                            when (theme) {
                                "light" -> R.string.theme_light
                                "dark" -> R.string.theme_dark
                                else -> R.string.theme_system
                            }
                        )
                    }
                }
            }
        }
        updateLanguageValue()
        showCoachmarkOnce(userPreferencesRepository, Coachmarks.SETTING) {
            listOf(
                CoachmarkStep(
                    { binding.settingCampus },
                    R.string.coachmark_setting_campus_title, R.string.coachmark_setting_campus_desc
                ),
                CoachmarkStep(
                    { binding.settingTheme },
                    R.string.coachmark_setting_theme_title, R.string.coachmark_setting_theme_desc
                ),
            )
        }
    }

    override fun onResume() {
        super.onResume()
        updateLanguageValue()
    }

    private fun updateLanguageValue() {
        val appLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language
            ?: resources.configuration.locales[0].language
        binding.settingLanguageValue.text = getString(
            when (appLanguage) {
                "en" -> R.string.language_english
                "ja" -> R.string.language_japanese
                "zh" -> R.string.language_chinese
                else -> R.string.language_korean
            }
        )
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
