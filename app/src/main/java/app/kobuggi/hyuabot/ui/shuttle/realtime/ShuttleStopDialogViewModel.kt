package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttleStopDialogQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShuttleStopDialogViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _result = MutableLiveData<ShuttleStopDialogQuery.Stop?>()
    val result get() = _result

    fun fetchData(stopID: String) {
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleStopDialogQuery(stopID)).execute()
            _result.value = response.data?.shuttle?.stop?.get(0)
        }
    }
}
