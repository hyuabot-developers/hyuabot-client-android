package app.kobuggi.hyuabot

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _showRemainingTime = MutableLiveData(true)
    private val _result = MutableLiveData<List<ShuttleRealtimePageQuery.Timetable>>()
    private val _stopInfo = MutableLiveData<List<ShuttleRealtimePageQuery.Stop>>()
    private val _disposable = CompositeDisposable()

    val result get() = _result
    val stopInfo get() = _stopInfo
    val isLoading get() = _isLoading
    val showRemainingTime get() = _showRemainingTime

    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val now = LocalDateTime.now()
        val currentTime = DateTimeFormatter.ofPattern("HH:mm").format(now)
        val currentDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(now)
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleRealtimePageQuery(currentTime, currentDateTime)).execute()
            _result.value = response.data?.shuttle?.timetable?.filter { it.time > currentTime }
            _stopInfo.value = response.data?.shuttle?.stop
            _isLoading.value = false
        }
    }

    fun start() {
        _disposable.add(
            Observable.interval(0, 1, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    try {
                        fetchData()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        )
    }

    fun stop() { _disposable.clear() }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
