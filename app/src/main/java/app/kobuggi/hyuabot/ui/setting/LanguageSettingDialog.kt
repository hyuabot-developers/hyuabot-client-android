package app.kobuggi.hyuabot.ui.setting
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogSettingLanguageBinding
import app.kobuggi.hyuabot.widget.refreshHyuabotWidgets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageSettingDialog @Inject constructor() : SettingChoiceDialogFragment() {
    private val binding by lazy { DialogSettingLanguageBinding.inflate(layoutInflater) }
    private val vm by viewModels<LanguageSettingDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {
            languageKorean.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_LANGUAGE, type = AnalyticsContentType.MENU, name = "korean")
                vm.setLocaleCode("ko")
            }
            languageEnglish.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_LANGUAGE, type = AnalyticsContentType.MENU, name = "english")
                vm.setLocaleCode("en")
            }
            languageJapanese.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_LANGUAGE, type = AnalyticsContentType.MENU, name = "japanese")
                vm.setLocaleCode("ja")
            }
            languageChinese.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_LANGUAGE, type = AnalyticsContentType.MENU, name = "chinese")
                vm.setLocaleCode("zh")
            }
        }
        vm.localeCode.observe(viewLifecycleOwner) {
            when (it) {
                "ko" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ko-KR"))
                "en" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en-US"))
                "ja" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ja-JP"))
                "zh" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zh-CN"))
            }
            val hostActivity = activity
            dismiss()
            refreshHyuabotWidgets(requireContext())
            hostActivity?.recreate()
        }
        return binding.root
    }
}
