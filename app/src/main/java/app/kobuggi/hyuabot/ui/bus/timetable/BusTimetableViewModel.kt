package app.kobuggi.hyuabot.ui.bus.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusTimetablePageQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusTimetableViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<BusTimetablePageQuery.Route>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val isLoading get() = _isLoading
    val queryError get() = _queryError

    fun fetchData(routeID: Int, stopID: Int) {
        if (_result.value == null) _isLoading.value = true
        viewModelScope.launch {
            val response = apolloClient.query(BusTimetablePageQuery(routeID, stopID)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.bus != null) {
                _result.value = response.data?.bus?.firstOrNull()?.routes?.firstOrNull()
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _isLoading.value = false
        }
    }
}
