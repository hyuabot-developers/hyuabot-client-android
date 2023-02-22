package app.kobuggi.hyuabot.ui.library

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.model.library.ReadingRoomItemResponse
import app.kobuggi.hyuabot.service.rest.APIService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(private val service: APIService) : ViewModel() {
    private val _rooms = MutableLiveData(listOf<ReadingRoomItemResponse>())
    private val _disposable = CompositeDisposable()
    private val _isLoading = MutableLiveData(false)
    private val _errorMessage = MutableLiveData(false)


    val rooms get() = _rooms
    val isLoading get() = _isLoading
    var campusID = 2
    val errorMessage get() = _errorMessage


    fun fetchData() {
        _errorMessage.value = false
        viewModelScope.launch {
            try {
                val response = service.readingRoomList(campusID)
                if (response.isSuccessful) {
                    _rooms.value = response.body()?.roomList ?: listOf()
                }
            } catch (e: Exception) {
                _errorMessage.value = true
            }
        }
    }

    fun start() {
        _disposable.add(
            Observable.interval(0, 1, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    try {
                        fetchData()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        )
    }

    fun stop() {
        _disposable.clear()
    }
}