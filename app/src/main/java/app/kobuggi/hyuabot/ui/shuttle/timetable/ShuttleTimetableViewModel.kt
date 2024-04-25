package app.kobuggi.hyuabot.ui.shuttle.timetable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ShuttleTimetablePageQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShuttleTimetableViewModel @Inject constructor(private val apolloClient: ApolloClient): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _result = MutableLiveData<List<ShuttleTimetablePageQuery.Timetable>>()

    val result get() = _result
    val isLoading get() = _isLoading

    val stopID: MutableLiveData<String?> = MutableLiveData(null)
    val tags: MutableLiveData<List<String>?> = MutableLiveData(null)
    val period: MutableLiveData<String?> = MutableLiveData(null)

    fun fetchData() {
        if (_result.value == null) _isLoading.value = true
        val periodQuery = if (period.value == null) {
            listOf()
        } else {
            listOf(period.value!!)
        }
        viewModelScope.launch {
            val response = apolloClient.query(ShuttleTimetablePageQuery(
                periodQuery,
                stopID.value ?: "",
                tags.value ?: listOf()
            )).execute()
            _isLoading.value = false
            _result.value = response.data?.shuttle?.timetable ?: emptyList()
        }
    }
}
