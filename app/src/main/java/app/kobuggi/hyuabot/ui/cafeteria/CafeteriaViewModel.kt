package app.kobuggi.hyuabot.ui.cafeteria

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.CafeteriaPageQuery
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaMenuSelector
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaRepository
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.time.CampusTime
import app.kobuggi.hyuabot.service.time.MealPeriod
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.cache.normalized.FetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CafeteriaViewModel @Inject constructor(
    private val cafeteriaRepository: CafeteriaRepository,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _date = MutableLiveData(CampusTime.now().toLocalDateTime())
    private val _breakfast = MutableLiveData<List<CafeteriaPageQuery.Cafeterium>>()
    private val _lunch = MutableLiveData<List<CafeteriaPageQuery.Cafeterium>>()
    private val _dinner = MutableLiveData<List<CafeteriaPageQuery.Cafeterium>>()
    private val _queryError = MutableLiveData<QueryError?>(null)
    private var lastRequest: Pair<LocalDate, Int>? = null

    val isLoading get() = _isLoading
    val date get() = _date
    val breakfast get() = _breakfast
    val lunch get() = _lunch
    val dinner get() = _dinner
    val campusID get() = userPreferencesRepository.campusID.asLiveData()
    val queryError get() = _queryError

    fun fetchData(campusID: Int = 2) {
        val date = (_date.value ?: CampusTime.now().toLocalDateTime()).toLocalDate()
        val request = date to campusID
        if (request == lastRequest && _queryError.value == null) return
        lastRequest = request

        viewModelScope.launch {
            _isLoading.value = true
            val result = cafeteriaRepository.fetchCafeterias(date, campusID, FetchPolicy.CacheFirst)
            if (lastRequest != request) return@launch
            result.fold(
                onSuccess = { list ->
                    _breakfast.value = CafeteriaMenuSelector.servingCafeterias(list, MealPeriod.BREAKFAST)
                    _lunch.value = CafeteriaMenuSelector.servingCafeterias(list, MealPeriod.LUNCH)
                    _dinner.value = CafeteriaMenuSelector.servingCafeterias(list, MealPeriod.DINNER)
                    _queryError.value = null
                },
                onFailure = { _queryError.value = QueryError.SERVER_ERROR },
            )
            _isLoading.value = false
        }
    }
}
