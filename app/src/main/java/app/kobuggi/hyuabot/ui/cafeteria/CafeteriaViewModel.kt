package app.kobuggi.hyuabot.ui.cafeteria

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CafeteriaViewModel @Inject constructor(): ViewModel() {
    private val _isLoading = MutableLiveData(false)

    val isLoading get() = _isLoading

    fun fetchData() {}
}
