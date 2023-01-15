package app.kobuggi.hyuabot.ui.menu.campus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class CampusDialogViewModel @Inject constructor() : ViewModel() {
    private val _campusCode = MutableLiveData<Int>()
    val campusCode get() = _campusCode

    fun setSeoul() {
        _campusCode.value = 1
    }

    fun setERICA() {
        _campusCode.value = 2
    }
}