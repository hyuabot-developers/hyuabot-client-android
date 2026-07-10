package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusRealtimePageQuery
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BusRealtimeViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<List<BusRealtimePageQuery.Bus>>()
    private val _notices = MutableLiveData<List<BusRealtimePageQuery.Notice1>>()

    private val _disposable = CompositeDisposable()
    private val _selectedStopID = MutableLiveData<Int?>(null)
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val notices get() = _notices
    val isLoading get() = _isLoading
    val selectedStopID get() = _selectedStopID
    val queryError get() = _queryError

    fun initSelectedStopID() {
        viewModelScope.launch {
            userPreferencesRepository.getBusStop().collect {
                _selectedStopID.value = it
            }
        }
    }

    fun setSelectedStopID(stopID: Int) {
        viewModelScope.launch { userPreferencesRepository.setBusStop(stopID) }
    }

    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val locale = AppCompatDelegate.getApplicationLocales().get(0)
        val appLanguage = locale?.language ?: Locale.getDefault().language
        val language = if (appLanguage == Locale.KOREAN.language) "KOREAN" else "ENGLISH"
        viewModelScope.launch {
            val response = apolloClient.query(BusRealtimePageQuery(language)).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.bus != null) {
                _result.value = response.data?.bus
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            if (_notices.value == null) {
                _notices.value = response.data?.notices?.flatMap { it.notices } ?: emptyList()
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
