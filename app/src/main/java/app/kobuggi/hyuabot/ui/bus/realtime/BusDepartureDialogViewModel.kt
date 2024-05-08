package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.BusDepartureLogDialogQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusDepartureDialogViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _result = MutableLiveData<List<BusDepartureLogDialogQuery.Log>>()
    val result get() = _result

    fun fetchData(stopID: Int, routeID: Int, dates: List<String> = listOf()) {
        viewModelScope.launch {
            val response = apolloClient.query(BusDepartureLogDialogQuery(stopID, routeID, dates)).execute()
            _result.value = response.data?.bus?.firstOrNull()?.routes?.firstOrNull()?.log ?: listOf()
        }
    }
}
