package app.kobuggi.hyuabot.ui.shuttle.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttlePeriodQuery
import app.kobuggi.hyuabot.ShuttleTimetablePageQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ShuttleTimetableViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<List<ShuttleTimetablePageQuery.Order>>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val isLoading get() = _isLoading
    val queryError get() = _queryError

    val stopID: MutableLiveData<String?> = MutableLiveData(null)
    val destinations: MutableLiveData<List<String>?> = MutableLiveData(null)
    val stopResID: MutableLiveData<Int?> = MutableLiveData(null)
    val headerResID: MutableLiveData<Int?> = MutableLiveData(null)
    val period: MutableLiveData<String?> = MutableLiveData(null)

    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        viewModelScope.launch {
            val periodQuery = if (period.value == null) {
                val period = apolloClient.query(ShuttlePeriodQuery(
                    date = LocalDate.now()
                )).fetchPolicy(FetchPolicy.CacheFirst).execute()
                if (period.data == null || period.exception != null) {
                    _queryError.value = QueryError.SERVER_ERROR
                    return@launch
                }
                if (period.data?.shuttle?.period != null) {
                    listOf(period.data?.shuttle?.period?.type!!)
                } else {
                    listOf()
                }
            } else {
                listOf(period.value!!)
            }
            val response = apolloClient.query(ShuttleTimetablePageQuery(
                periodQuery,
                stopID.value ?: "",
                destinations.value ?: listOf()
            )).fetchPolicy(FetchPolicy.CacheFirst).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (!response.data?.shuttle?.stops.isNullOrEmpty()) {
                _result.value = response.data?.shuttle?.stops?.first()?.timetable?.order ?: emptyList()
                _queryError.value = null
            }
            _isLoading.value = false
        }
    }
}
