package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttleStopDialogQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ShuttleStopDialogViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _result = MutableLiveData<ShuttleStopDialogQuery.Stop?>()
    private val _departureList = MutableLiveData<List<ShuttleStopDialogQuery.Timetable>>()
    val result get() = _result
    val departureList get() = _departureList

    fun fetchData(stopID: String) {
        val now = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleStopDialogQuery(stopID, now)).execute()
            _result.value = response.data?.shuttle?.stop?.get(0)
            _departureList.value = response.data?.shuttle?.timetable
        }
    }
}
