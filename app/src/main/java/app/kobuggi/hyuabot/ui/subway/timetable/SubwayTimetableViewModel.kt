package app.kobuggi.hyuabot.ui.subway.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.SubwayTimetablePageQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubwayTimetableViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _timetable = MutableLiveData<List<SubwayTimetablePageQuery.Timetable>>()
    private val _heading = MutableLiveData<String>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val isLoading get() = _isLoading
    val timetable get() = _timetable
    val heading get() = _heading
    val queryError get() = _queryError

    fun fetchData(stationID: String, heading: String) {
        _heading.value = heading
        viewModelScope.launch {
            val response = apolloClient.query(SubwayTimetablePageQuery(stationID, listOf(heading))).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.subway != null) {
                _timetable.value = response.data?.subway?.firstOrNull()?.timetable ?: listOf()
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _isLoading.value = false
        }
    }
}
