package app.kobuggi.hyuabot.ui.bus.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusRouteInfoDialogQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusRouteDialogViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _result = MutableLiveData<BusRouteInfoDialogQuery.Route>()
    private val _isLoading = MutableLiveData(true)
    private val _queryError = MutableLiveData<QueryError?>(null)

    val busRoute get() = _result
    val isLoading get() = _isLoading
    val queryError get() = _queryError

    fun fetchData(stopID: Int, routeID: Int) {
        viewModelScope.launch {
            val response = apolloClient.query(BusRouteInfoDialogQuery(routeID, stopID)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.bus != null) {
                val route = response.data?.bus?.firstOrNull()?.routes?.firstOrNull()
                if (route != null) { _result.value = route }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _isLoading.value = false
        }
    }
}
