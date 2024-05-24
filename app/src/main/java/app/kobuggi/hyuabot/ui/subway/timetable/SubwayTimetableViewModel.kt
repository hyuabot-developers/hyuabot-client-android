package app.kobuggi.hyuabot.ui.subway.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.SubwayTimetablePageDownQuery
import app.kobuggi.hyuabot.SubwayTimetablePageUpQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubwayTimetableViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _up = MutableLiveData<List<SubwayTimetablePageUpQuery.Up>>()
    private val _down = MutableLiveData<List<SubwayTimetablePageDownQuery.Down>>()
    private val _heading = MutableLiveData<String>()

    val isLoading get() = _isLoading
    val up get() = _up
    val down get() = _down
    val heading get() = _heading

    fun fetchData(stationID: String, heading: String) {
        _heading.value = heading
        viewModelScope.launch {
            if (heading == "up") {
                val response = try {
                    apolloClient.query(SubwayTimetablePageUpQuery(stationID)).execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                _up.value = response?.data?.subway?.first()?.timetable?.up
            } else {
                val response = try {
                    apolloClient.query(SubwayTimetablePageDownQuery(stationID)).execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                _down.value = response?.data?.subway?.first()?.timetable?.down
            }
            _isLoading.value = false
        }
    }
}
