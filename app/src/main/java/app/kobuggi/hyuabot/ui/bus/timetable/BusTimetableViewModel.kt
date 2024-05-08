package app.kobuggi.hyuabot.ui.bus.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusTimetablePageQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusTimetableViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<List<BusTimetablePageQuery.Timetable>>()

    val result get() = _result
    val isLoading get() = _isLoading

    fun fetchData(routeID: Int, stopID: Int) {
        if (_result.value == null) _isLoading.value = true
        viewModelScope.launch {
            val response = apolloClient.query(BusTimetablePageQuery(routeID, stopID)).execute()
            _result.value = response.data?.bus?.firstOrNull()?.routes?.firstOrNull()?.timetable
            _isLoading.value = false
        }
    }
}
