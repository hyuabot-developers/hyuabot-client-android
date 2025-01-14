package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttleStopDialogQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ShuttleStopDialogViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _result = MutableLiveData<ShuttleStopDialogQuery.Stop?>()
    private val _departureList = MutableLiveData<List<ShuttleStopDialogQuery.Timetable>>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val departureList get() = _departureList
    val queryError get() = _queryError

    fun fetchData(stopID: String) {
        val now = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleStopDialogQuery(stopID, now)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.shuttle?.stop != null) {
                _result.value = response.data?.shuttle?.stop?.get(0)
                _departureList.value = response.data?.shuttle?.timetable
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
        }
    }
}
