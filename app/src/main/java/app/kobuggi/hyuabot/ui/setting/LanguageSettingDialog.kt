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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
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
            languageKorean.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_LANGUAGE, type = AnalyticsContentType.MENU, name = "korean")
                vm.setLocaleCode("ko")
            }
            languageEnglish.setOnClickListener {
                AnalyticsManager.logSelect(AnalyticsItem.SETTING_SELECT_LANGUAGE, type = AnalyticsContentType.MENU, name = "english")
                vm.setLocaleCode("en")
            }
        }
        vm.localeCode.observe(viewLifecycleOwner) {
            if (it == "ko") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ko-KR"))
            } else if (it == "en") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en-US"))
            }
            dismiss()
        }
        return binding.root
    }
}
