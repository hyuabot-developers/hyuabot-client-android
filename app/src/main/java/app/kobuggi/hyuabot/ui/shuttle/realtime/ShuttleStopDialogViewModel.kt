package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttlePeriodQuery
import app.kobuggi.hyuabot.ShuttleStopDialogQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ShuttleStopDialogViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _result = MutableLiveData<ShuttleStopDialogQuery.Stop?>()
    private val _departureList = MutableLiveData<List<ShuttleStopDialogQuery.Destination>>()
    private val _queryError = MutableLiveData<QueryError?>(null)
    private val _period: MutableLiveData<String?> = MutableLiveData(null)
    val result get() = _result
    val departureList get() = _departureList
    val queryError get() = _queryError
    val period get() = _period

    fun fetchData(stopID: String) {
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
            val response = apolloClient.query(ShuttleStopDialogQuery(stopID, periodQuery)).fetchPolicy(FetchPolicy.CacheFirst).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.shuttle?.stops != null) {
                val stop = response.data?.shuttle?.stops?.firstOrNull()
                _result.value = stop
                _departureList.value = stop?.timetable?.destination ?: emptyList()
                _queryError.value = if (stop == null) QueryError.UNKNOWN_ERROR else null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
        }
    }
}
