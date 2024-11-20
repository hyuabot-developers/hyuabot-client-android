package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo3.ApolloClient
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
class ShuttleRealtimeViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _showRemainingTime = MutableLiveData(true)
    private val _result = MutableLiveData<List<ShuttleRealtimePageQuery.Timetable>>()
    private val _stopInfo = MutableLiveData<List<ShuttleRealtimePageQuery.Stop>>()
    private val _disposable = CompositeDisposable()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val stopInfo get() = _stopInfo
    val isLoading get() = _isLoading
    val showRemainingTime get() = _showRemainingTime
    val queryError get() = _queryError

    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val now = LocalDateTime.now()
        val currentTime = DateTimeFormatter.ofPattern("HH:mm").format(now)
        val currentDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(now)
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleRealtimePageQuery(currentTime, currentDateTime)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.shuttle?.timetable != null) {
                _result.value = response.data?.shuttle?.timetable?.filter { it.time > currentTime }
                _stopInfo.value = response.data?.shuttle?.stop
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _isLoading.value = false
        }
    }

    fun setRemainingTimeVisibility(isVisible: Boolean) {
        _showRemainingTime.value = isVisible
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
