package app.kobuggi.hyuabot.ui.menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor() : ViewModel() {
    private val _moveEvent = MutableLiveData(0)
    val moveEvent get() = _moveEvent

    fun moveToSomewhere(stringID: Int) {
        moveEvent.value = stringID
    }
}

