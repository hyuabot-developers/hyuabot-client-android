package app.kobuggi.hyuabot.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.HomePageQuery
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.type.BusRouteStopInput
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _data = MutableLiveData<HomePageQuery.Data?>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val isLoading: LiveData<Boolean> get() = _isLoading
    val data: LiveData<HomePageQuery.Data?> get() = _data
    val queryError: LiveData<QueryError?> get() = _queryError

    fun fetchData() {
        viewModelScope.launch {
            if (_data.value == null) _isLoading.value = true
            val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
            val mealDate = if (now.hour >= 20) now.toLocalDate().plusDays(1) else now.toLocalDate()
            val response = apolloClient.query(
                HomePageQuery(
                    after = Optional.present(LocalTime.now(ZoneId.of("Asia/Seoul"))),
                    weekday = currentSubwayWeekday(now),
                    date = mealDate,
                    campusID = userPreferencesRepository.campusID.first(),
                    busInput = homeBusInput(),
                )
            ).fetchPolicy(FetchPolicy.NetworkOnly).execute()

            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else {
                _data.value = response.data
                _queryError.value = null
            }
            _isLoading.value = false
        }
    }

    private fun currentSubwayWeekday(now: ZonedDateTime): String {
        return if (now.dayOfWeek == DayOfWeek.SATURDAY || now.dayOfWeek == DayOfWeek.SUNDAY) {
            "weekends"
        } else {
            "weekdays"
        }
    }

    private fun homeBusInput(): List<BusRouteStopInput> = listOf(
        BusRouteStopInput(route = 216000068, stop = 216000383, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000068, stop = 216000138, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000081, stop = 216000028, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000101, stop = 216000028, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000016, stop = 216000152, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000082, stop = 216000077, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000102, stop = 216000077, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000016, stop = 216000074, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000082, stop = 217000140, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000102, stop = 217000140, limit = Optional.present(1)),
        BusRouteStopInput(route = 216000016, stop = 217000264, limit = Optional.present(1)),
    )
}
