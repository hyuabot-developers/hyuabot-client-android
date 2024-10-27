package app.kobuggi.hyuabot.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanguageSettingDialogViewModel @Inject constructor() : ViewModel() {
    private val _localeCode = MutableLiveData<String>()
    val localeCode get() = _localeCode

    fun setLocaleCode(code: String) {
        _localeCode.value = code
    }
}
