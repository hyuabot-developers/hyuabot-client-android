package app.kobuggi.hyuabot.ui.subway.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.SubwayRealtimePageQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SubwayRealtimeViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _campusYellow = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _campusBlue = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _queryError = MutableLiveData<QueryError?>(null)
    private val _disposable = CompositeDisposable()

    val isLoading get() = _isLoading
    val queryError get() = _queryError
    val campusYellow get() = _campusYellow
    val campusBlue get() = _campusBlue

    fun fetchData() {
        viewModelScope.launch {
            val response = apolloClient.query(SubwayRealtimePageQuery()).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.subway != null) {
                _campusYellow.value = response.data?.subway?.first { it.stationID == "K251" }
                _campusBlue.value = response.data?.subway?.first { it.stationID == "K449" }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
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
