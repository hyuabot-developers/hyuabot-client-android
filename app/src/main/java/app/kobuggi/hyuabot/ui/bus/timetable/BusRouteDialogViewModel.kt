package app.kobuggi.hyuabot.ui.bus.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusRouteInfoDialogQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusRouteDialogViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _result = MutableLiveData<BusRouteInfoDialogQuery.Route>()
    private val _isLoading = MutableLiveData(true)
    val busRoute get() = _result
    val isLoading get() = _isLoading

    fun fetchData(stopID: Int, routeID: Int) {
        viewModelScope.launch {
            val response = apolloClient.query(BusRouteInfoDialogQuery(routeID, stopID)).execute()
            val route = response.data?.bus?.firstOrNull()?.routes?.firstOrNull()
            if (route != null) {
                _result.value = route
                _isLoading.value = false
            }
        }
    }
}
