package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusDepartureLogDialogQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusDepartureDialogViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _result = MutableLiveData<List<BusDepartureLogDialogQuery.Route>>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val result get() = _result
    val queryError get() = _queryError

    fun fetchData(stopID: Int, routes: List<Int>, dates: List<String> = listOf()) {
        viewModelScope.launch {
            val response = apolloClient.query(BusDepartureLogDialogQuery(stopID, routes, dates)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.bus != null) {
                _result.value = response.data?.bus?.firstOrNull()?.routes
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
        }
    }
}
