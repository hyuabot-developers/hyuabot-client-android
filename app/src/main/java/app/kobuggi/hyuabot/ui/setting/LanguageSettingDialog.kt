package app.kobuggi.hyuabot.ui.setting
import app.kobuggi.hyuabot.util.AnalyticsContentType
import app.kobuggi.hyuabot.util.AnalyticsItem
import app.kobuggi.hyuabot.util.AnalyticsManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogSettingLanguageBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageSettingDialog @Inject constructor() : DialogFragment() {
    private val binding by lazy { DialogSettingLanguageBinding.inflate(layoutInflater) }
    private val vm by viewModels<LanguageSettingDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.apply {
            val currentLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language ?: java.util.Locale.getDefault().language
            markSelected(languageKorean, currentLanguage == "ko")
            markSelected(languageEnglish, currentLanguage == "en")
            markSelected(languageJapanese, currentLanguage == "ja")
            markSelected(languageChinese, currentLanguage == "zh")
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
            hostActivity?.recreate()
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
