package app.kobuggi.hyuabot.ui.subway.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.SubwayRealtimePageQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SubwayRealtimeViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _campusYellow = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _campusBlue = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _oidoYellow = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _oidoBlue = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _chojiSeohae = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _queryError = MutableLiveData<QueryError?>(null)
    private val _disposable = CompositeDisposable()

    val isLoading get() = _isLoading
    val queryError get() = _queryError
    val campusYellow get() = _campusYellow
    val campusBlue get() = _campusBlue
    val oidoYellow get() = _oidoYellow
    val oidoBlue get() = _oidoBlue
    val chojiSeohae get() = _chojiSeohae
    val combinedData get() = combine(
        campusYellow.asFlow(),
        campusBlue.asFlow(),
        oidoYellow.asFlow(),
        oidoBlue.asFlow(),
        chojiSeohae.asFlow()
    ) { campusYellow, campusBlue, oidoYellow, oidoBlue, chojiSeohae ->
        SubwayRealtimeCombinedData(campusYellow, campusBlue, oidoYellow, oidoBlue, chojiSeohae)
    }.onStart { emit(SubwayRealtimeCombinedData(null, null, null, null, null)) }.asLiveData()

    fun fetchData() {
        val localDate = LocalDate.now()
        viewModelScope.launch {
            val response = apolloClient.query(SubwayRealtimePageQuery(
                weekday = if (localDate.dayOfWeek.value in 1..5) "weekdays" else "weekends"
            )).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.subway != null) {
                _campusYellow.value = response.data?.subway?.firstOrNull { it.stationID == "K251" }
                _campusBlue.value = response.data?.subway?.firstOrNull { it.stationID == "K449" }
                _oidoYellow.value = response.data?.subway?.firstOrNull { it.stationID == "K258" }
                _oidoBlue.value = response.data?.subway?.firstOrNull { it.stationID == "K456" }
                _chojiSeohae.value = response.data?.subway?.firstOrNull { it.stationID == "S26" }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _isLoading.value = false
        }
    }

    fun start() {
        if (_disposable.size() > 0) return
        _disposable.add(
            Observable.interval(0, 15, TimeUnit.SECONDS)
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
        stop()
    }
}
