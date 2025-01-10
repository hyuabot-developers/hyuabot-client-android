package app.kobuggi.hyuabot.ui.shuttle.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttlePeriodQuery
import app.kobuggi.hyuabot.ShuttleTimetablePageQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShuttleTimetableViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<List<ShuttleTimetablePageQuery.Timetable>>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val isLoading get() = _isLoading
    val queryError get() = _queryError

    val stopID: MutableLiveData<String?> = MutableLiveData(null)
    val tags: MutableLiveData<List<String>?> = MutableLiveData(null)
    val stopResID: MutableLiveData<Int?> = MutableLiveData(null)
    val headerResID: MutableLiveData<Int?> = MutableLiveData(null)
    val period: MutableLiveData<String?> = MutableLiveData(null)

    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        viewModelScope.launch {
            val periodQuery = if (period.value == null) {
                val period = apolloClient.query(ShuttlePeriodQuery()).execute()
                if (period.data == null || period.exception != null) {
                    _queryError.value = QueryError.SERVER_ERROR
                    return@launch
                }
                if (period.data?.shuttle?.period?.firstOrNull() != null) {
                    listOf(period.data?.shuttle?.period?.first()!!.type)
                } else {
                    listOf()
                }
            } else {
                listOf(period.value!!)
            }
            val response = apolloClient.query(ShuttleTimetablePageQuery(
                periodQuery,
                stopID.value ?: "",
                tags.value ?: listOf()
            )).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.shuttle?.timetable != null) {
                _result.value = response.data?.shuttle?.timetable ?: emptyList()
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _isLoading.value = false
        }
    }
}
