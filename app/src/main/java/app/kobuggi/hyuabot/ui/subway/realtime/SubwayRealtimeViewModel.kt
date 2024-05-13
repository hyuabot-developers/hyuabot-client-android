package app.kobuggi.hyuabot.ui.subway.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.SubwayRealtimePageQuery
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
class SubwayRealtimeViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _K251 = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _K258 = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _K449 = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _K456 = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _disposable = CompositeDisposable()

    val isLoading get() = _isLoading
    val K251 get() = _K251
    val K258 get() = _K258
    val K449 get() = _K449
    val K456 get() = _K456

    fun fetchData() {
        val now = LocalDateTime.now()
        val currentTime = DateTimeFormatter.ofPattern("HH:mm").format(now)
        viewModelScope.launch {
            val response = try {
                apolloClient.query(SubwayRealtimePageQuery(currentTime)).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            _K251.value = response?.data?.subway?.first { it.id == "K251" }
            _K258.value = response?.data?.subway?.first { it.id == "K258" }
            _K449.value = response?.data?.subway?.first { it.id == "K449" }
            _K456.value = response?.data?.subway?.first { it.id == "K456" }
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
