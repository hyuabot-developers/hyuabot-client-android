package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ShuttleRealtimeViewModel @Inject constructor(
    val userPreferencesRepository: UserPreferencesRepository,
    private val apolloClient: ApolloClient
): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _showDepartureTime = MutableLiveData(false)
    private val _showByDestination = MutableLiveData(false)
    private val _showRemainingTime = MutableLiveData(true)
    private val _result = MutableLiveData<List<ShuttleRealtimePageQuery.Stop>>()
    private val _notices = MutableLiveData<List<ShuttleRealtimePageQuery.Notice1>>()
    private val _disposable = CompositeDisposable()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val notices get() = _notices
    val isLoading get() = _isLoading
    val queryError get() = _queryError
    val showDepartureTime get() = _showDepartureTime
    val showByDestination get() = _showByDestination
    val latestShuttleResult = combine(result.asFlow(), showByDestination.asFlow()) { result, showByDestination ->
        ShuttleTabData(result, showByDestination)
    }.onStart { emit(ShuttleTabData(listOf(), false)) }.asLiveData()


    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val locale = AppCompatDelegate.getApplicationLocales().get(0)
        val language = if (locale == null) {
            "KOREAN"
        } else {
            when (locale.language) {
                "ko" -> "KOREAN"
                "en" -> "ENGLISH"
                else -> "KOREAN"
            }
        }
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleRealtimePageQuery(
                language,
                Optional.present(LocalTime.now())
            )).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.shuttle?.stops != null) {
                _result.value = response.data?.shuttle?.stops
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

    fun setShowDepartureTime(isVisible: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setShowShuttleDepartureTime(isVisible) }
    }

    fun setShowByDestination(isVisible: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setShowShuttleByDestination(isVisible) }
    }

    fun stop() { _disposable.clear() }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
