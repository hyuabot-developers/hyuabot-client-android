package app.kobuggi.hyuabot.ui.cafeteria

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CafeteriaViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _date = MutableLiveData(LocalDateTime.now())
    private val _breakfast = MutableLiveData<List<CafeteriaPageQuery.Menu>>()
    private val _lunch = MutableLiveData<List<CafeteriaPageQuery.Menu>>()
    private val _dinner = MutableLiveData<List<CafeteriaPageQuery.Menu>>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val isLoading get() = _isLoading
    val date get() = _date
    val breakfast get() = _breakfast
    val lunch get() = _lunch
    val dinner get() = _dinner
    val campusID get() = userPreferencesRepository.campusID.asLiveData()
    val queryError get() = _queryError

    fun fetchData(campusID: Int = 2) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = apolloClient.query(CafeteriaPageQuery(_date.value?.toLocalDate().toString(), campusID)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.menu != null) {
                _breakfast.value = response.data?.menu?.filter {
                    it.menu.any { menuItem -> menuItem.type.contains("조식") }
                }
                _lunch.value = response.data?.menu?.filter {
                    it.menu.any { menuItem -> menuItem.type.contains("중식") }
                }
                _dinner.value = response.data?.menu?.filter {
                    it.menu.any { menuItem -> menuItem.type.contains("석식") }
                }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _isLoading.value = false
        }
    }
}
