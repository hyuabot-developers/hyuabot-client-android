package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusStopDialogQuery
import app.kobuggi.hyuabot.type.BusRouteStopInput
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusStopInfoViewModel @Inject constructor(
    private val apolloClient: ApolloClient
) : ViewModel() {
    private val _result = MutableLiveData<List<BusStopDialogQuery.Bus>>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result: LiveData<List<BusStopDialogQuery.Bus>> get() = _result
    val queryError: LiveData<QueryError?> get() = _queryError

    fun fetchData(stopID: Int, routeIDs: List<Int>) {
        viewModelScope.launch {
            val routeStops = routeIDs.filter { it != 0 }.map { BusRouteStopInput(route = it, stop = stopID) }
            if (routeStops.isEmpty()) return@launch
            val response = apolloClient.query(BusStopDialogQuery(routeStops))
                .fetchPolicy(FetchPolicy.NetworkOnly)
                .execute()
            if (response.data != null && response.exception == null) {
                _result.value = response.data?.bus ?: emptyList()
            } else {
                _queryError.value = QueryError.SERVER_ERROR
            }
        }
    }
}
