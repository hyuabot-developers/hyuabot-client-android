package app.kobuggi.hyuabot.ui.setting

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.ui.common.coachmark.Coachmarks
import app.kobuggi.hyuabot.ui.common.compose.HyuabotTheme
import app.kobuggi.hyuabot.ui.common.openCustomTab
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager
import app.kobuggi.hyuabot.util.CrashlyticsManager
import app.kobuggi.hyuabot.widget.refreshHyuabotWidgets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment @Inject constructor() : Fragment() {
    private val viewModel by viewModels<SettingViewModel>()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { HyuabotTheme { SettingContent() } }
        }

    @Composable
    private fun SettingContent() {
        val campusID by viewModel.campusID.collectAsStateWithLifecycle()
        val theme by viewModel.theme.collectAsStateWithLifecycle()
        val startScreen by viewModel.startScreen.collectAsStateWithLifecycle()
        val analytics by viewModel.analyticsConsent.collectAsStateWithLifecycle()
        val languageCode = currentLanguageCode()
        var showSheet by remember { mutableStateOf(false) }
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val systemLanguage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val resetSnack = getString(R.string.coachmark_reset_complete)

        val state = SettingState(
            campusID = campusID,
            theme = theme,
            startScreen = startScreen,
            languageName = stringResource(AppLanguage.entries.firstOrNull { it.code == languageCode }?.localized ?: R.string.language_korean),
            analytics = analytics,
            versionName = BuildConfig.VERSION_NAME,
            systemLanguageSettings = systemLanguage,
        )
        val actions = SettingActions(
            onBack = { findNavController().popBackStack() },
            onCampus = { id ->
                if (id != campusID) {
                    log("campus")
                    AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_CAMPUS, type = AnalyticsContentType.MENU, name = if (id == 1) "seoul" else "erica")
                    viewModel.setCampusID(id)
                    refreshHyuabotWidgets(requireContext())
                    val name = getString(if (id == 1) R.string.campus_seoul else R.string.campus_erica)
                    scope.launch { snackbar.showSnackbar(getString(R.string.setting_campus_changed_format, name)) }
                }
            },
            onTheme = { value ->
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_THEME, type = AnalyticsContentType.MENU, name = value ?: "system")
                viewModel.setTheme(value)
            },
            onStartScreen = { viewModel.setStartScreen(it) },
            onLanguage = {
                log("language")
                if (systemLanguage) openSystemLanguageSettings() else showSheet = true
            },
            onAnalytics = { enabled ->
                viewModel.setAnalyticsConsent(enabled)
                AnalyticsManager.setCollectionEnabled(enabled)
                CrashlyticsManager.setCollectionEnabled(enabled)
            },
            onPrivacy = { log("privacy_policy"); requireContext().openCustomTab(getString(R.string.privacy_policy_url)) },
            onHelp = { log("help"); requireContext().openCustomTab(helpUrl()) },
            onResetCoachmarks = {
                viewLifecycleOwner.lifecycleScope.launch {
                    Coachmarks.USER_VISIBLE_KEYS.forEach { userPreferencesRepository.resetCoachmark(it) }
                    snackbar.showSnackbar(resetSnack)
                }
            },
            onLicenses = { log("open_source_licenses"); findNavController().navigate(R.id.licensesFragment) },
            onVersionLongPress = {
                viewLifecycleOwner.lifecycleScope.launch {
                    userPreferencesRepository.resetCoachmark(Coachmarks.SHUTTLE_REALTIME_UPDATES)
                    Toast.makeText(requireContext(), R.string.coachmark_shuttle_realtime_updates_reset, Toast.LENGTH_SHORT).show()
                }
            },
        )
        Box {
            SettingScreen(state, actions)
            SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
            if (showSheet) {
                LanguageSheet(
                    currentCode = languageCode,
                    onSelect = { language ->
                        AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_LANGUAGE, type = AnalyticsContentType.MENU, name = language.analyticsName)
                        showSheet = false
                        applyLanguage(language)
                    },
                    onDismiss = { showSheet = false },
                )
            }
        }
    }

    private fun log(name: String) =
        AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_ROW, type = AnalyticsContentType.LIST_ITEM, name = name)

    private fun currentLanguageCode(): String =
        AppCompatDelegate.getApplicationLocales()[0]?.language ?: resources.configuration.locales[0].language

    private fun applyLanguage(language: AppLanguage) {
        val host = activity
        viewModel.clearLocalizedCaches {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
            refreshHyuabotWidgets(requireContext())
            host?.recreate()
        }
    }

    private fun openSystemLanguageSettings() {
        val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", requireContext().packageName, null))
        runCatching { startActivity(intent) }
    }

    private fun helpUrl(): String {
        val docsLocalePath = when (currentLanguageCode()) {
            "en" -> "en/"
            "ja" -> "ja/"
            "zh" -> "zh-Hans/"
            else -> ""
        }
        return "${getString(R.string.docs_site_base_url)}/$docsLocalePath" + "docs/android"
    }

}
