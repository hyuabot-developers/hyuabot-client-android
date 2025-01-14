package app.kobuggi.hyuabot.ui.subway.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.SubwayTimetablePageDownQuery
import app.kobuggi.hyuabot.SubwayTimetablePageUpQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubwayTimetableViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _up = MutableLiveData<List<SubwayTimetablePageUpQuery.Up>>()
    private val _down = MutableLiveData<List<SubwayTimetablePageDownQuery.Down>>()
    private val _heading = MutableLiveData<String>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val isLoading get() = _isLoading
    val up get() = _up
    val down get() = _down
    val heading get() = _heading
    val queryError get() = _queryError

    fun fetchData(stationID: String, heading: String) {
        _heading.value = heading
        viewModelScope.launch {
            if (heading == "up") {
                val response = apolloClient.query(SubwayTimetablePageUpQuery(stationID)).execute()
                if (response.data == null || response.exception != null) {
                    _queryError.value = QueryError.SERVER_ERROR
                } else if (response.data?.subway != null) {
                    _up.value = response.data?.subway?.first()?.timetable?.up
                    _queryError.value = null
                } else {
                    _queryError.value = QueryError.UNKNOWN_ERROR
                }
            } else {
                val response = apolloClient.query(SubwayTimetablePageDownQuery(stationID)).execute()
                if (response.data == null || response.exception != null) {
                    _queryError.value = QueryError.SERVER_ERROR
                } else if (response.data?.subway != null) {
                    _down.value = response.data?.subway?.first()?.timetable?.down
                    _queryError.value = null
                } else {
                    _queryError.value = QueryError.UNKNOWN_ERROR
                }
            }
            _isLoading.value = false
        }
    }
}
