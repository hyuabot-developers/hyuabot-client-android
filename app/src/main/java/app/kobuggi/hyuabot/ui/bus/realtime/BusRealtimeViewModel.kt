package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusRealtimePageQuery
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
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
class BusRealtimeViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
    init {
        viewModelScope.launch {
            userPreferencesRepository.getBusStop().collect {
                _selectedStopID.value = it
            }
        }
    }

    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<List<BusRealtimePageQuery.Bus>>()
    private val _disposable = CompositeDisposable()
    private val _selectedStopID = MutableLiveData<Int?>(null)
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val isLoading get() = _isLoading
    val selectedStopID get() = _selectedStopID
    val queryError get() = _queryError

    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val now = LocalDateTime.now()
        val currentTime = DateTimeFormatter.ofPattern("HH:mm").format(now)
        viewModelScope.launch {
            val response = apolloClient.query(BusRealtimePageQuery(currentTime)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.bus != null) {
                _result.value = response.data?.bus
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
