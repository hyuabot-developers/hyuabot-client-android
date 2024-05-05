package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusRealtimePageQuery
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
class BusRealtimeViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<List<BusRealtimePageQuery.Bus>>()
    private val _disposable = CompositeDisposable()

    val result get() = _result
    val isLoading get() = _isLoading

    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val now = LocalDateTime.now()
        val currentTime = DateTimeFormatter.ofPattern("HH:mm").format(now)
        val weekdays = when (now.dayOfWeek.value) {
            1, 2, 3, 4, 5 -> "weekdays"
            6 -> "saturday"
            7 -> "sunday"
            else -> "weekdays"
        }
        viewModelScope.launch {
            val response = apolloClient.query(BusRealtimePageQuery(currentTime, weekdays)).execute()
            _result.value = response.data?.bus
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
