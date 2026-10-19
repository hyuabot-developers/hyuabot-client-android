package app.kobuggi.hyuabot.ui.subway.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.SubwayRealtimePageQuery
import app.kobuggi.hyuabot.type.SubwayStationInput
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator
import app.kobuggi.hyuabot.util.QueryError
import app.kobuggi.hyuabot.ui.common.RequestUiState
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
import kotlinx.coroutines.CancellationException
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SubwayRealtimeViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    val sectionUpdatedAts = MutableLiveData<Map<String, Instant?>>(emptyMap())

    fun reportSectionUpdatedAt(page: Int, sectionKey: String, at: Instant?) {
        val key = "$page:$sectionKey"
        sectionUpdatedAts.value = sectionUpdatedAts.value.orEmpty() + (key to at)
    }

    private val _isLoading = MutableLiveData(false)
    private val _campusYellow = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _campusBlue = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _oidoYellow = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _oidoBlue = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _chojiSeohae = MutableLiveData<SubwayRealtimePageQuery.Subway?>()
    private val _queryError = MutableLiveData<QueryError?>(null)
    private val _requestState = MutableLiveData(RequestUiState())
    private val _disposable = CompositeDisposable()
    private var loadedLanguage: String? = null
    private var requestGeneration = 0
    private var lastAppliedGeneration = 0
    private var lastAppliedRequestKey: Pair<List<SubwayStationInput>, String>? = null

    private fun clearData() {
        _campusYellow.value = null
        _campusBlue.value = null
        _oidoYellow.value = null
        _oidoBlue.value = null
        _chojiSeohae.value = null
    }

    val isLoading get() = _isLoading
    val queryError get() = _queryError
    val requestState: LiveData<RequestUiState> get() = _requestState
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
        val language = DynamicTextTranslator.currentAppLanguageTag()
        if (loadedLanguage != language) {
            clearData()
            loadedLanguage = language
        }
        val generation = ++requestGeneration
        val keys = subwayRequestKeys(if (localDate.dayOfWeek.value in 1..5) "weekdays" else "weekends")
        val requestKey = keys to language
        val retainSnapshot = requestKey == lastAppliedRequestKey && _requestState.value?.hasSnapshot == true
        _requestState.value = (_requestState.value ?: RequestUiState()).start(retainSnapshot)
        if (!retainSnapshot) {
            clearData()
            _isLoading.value = true
        }
        viewModelScope.launch {
            try {
            val response = apolloClient.query(SubwayRealtimePageQuery(
                keys = keys,
                language = language,
            )).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            val currentDate = LocalDate.now()
            val currentWeekday = if (currentDate.dayOfWeek.value in 1..5) "weekdays" else "weekends"
            if (generation != requestGeneration || generation <= lastAppliedGeneration ||
                keys != subwayRequestKeys(currentWeekday) ||
                language != DynamicTextTranslator.currentAppLanguageTag()
            ) return@launch
            if (response.data == null || response.exception != null || response.hasErrors()) {
                _queryError.value = QueryError.SERVER_ERROR
                _requestState.value = _requestState.value?.failure() ?: RequestUiState().failure()
            } else if (response.data?.subway != null) {
                lastAppliedGeneration = generation
                lastAppliedRequestKey = requestKey
                _campusYellow.value = response.data?.subway?.firstOrNull { it.stationID == "K251" }
                _campusBlue.value = response.data?.subway?.firstOrNull { it.stationID == "K449" }
                _oidoYellow.value = response.data?.subway?.firstOrNull { it.stationID == "K258" }
                _oidoBlue.value = response.data?.subway?.firstOrNull { it.stationID == "K456" }
                _chojiSeohae.value = response.data?.subway?.firstOrNull { it.stationID == "S26" }
                _requestState.value = _requestState.value?.success() ?: RequestUiState().success()
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
                _requestState.value = _requestState.value?.failure() ?: RequestUiState().failure()
            }
            _isLoading.value = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (generation != requestGeneration || generation <= lastAppliedGeneration || language != DynamicTextTranslator.currentAppLanguageTag()) return@launch
                _queryError.value = QueryError.SERVER_ERROR
                _requestState.value = _requestState.value?.failure() ?: RequestUiState().failure()
                _isLoading.value = false
            }
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
