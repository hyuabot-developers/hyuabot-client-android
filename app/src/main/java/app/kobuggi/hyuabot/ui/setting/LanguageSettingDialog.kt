package app.kobuggi.hyuabot.ui.setting

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.DialogSettingLanguageBinding
import app.kobuggi.hyuabot.ui.MainActivity
import app.kobuggi.hyuabot.util.LocaleUtility
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
        val sharedPreferences = requireActivity().getSharedPreferences("hyuabot", 0)
        binding.apply {
            languageKorean.setOnClickListener {
                vm.setLocaleCode("ko")
            }
            languageEnglish.setOnClickListener {
                vm.setLocaleCode("en")
            }
        }
        vm.localeCode.observe(viewLifecycleOwner) {
            LocaleUtility.setLocale(it)
            sharedPreferences.edit().apply {
                putString("locale", it)
                apply()
            }
            dismiss()
        }
        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (requireActivity() is MainActivity){
            (requireActivity() as MainActivity).onDismiss(dialog)
        }
    }
}
