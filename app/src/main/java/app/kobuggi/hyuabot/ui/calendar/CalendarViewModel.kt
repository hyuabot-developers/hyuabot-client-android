package app.kobuggi.hyuabot.ui.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.CalendarPageQuery
import app.kobuggi.hyuabot.CalendarPageVersionQuery
import app.kobuggi.hyuabot.service.database.AppDatabase
import app.kobuggi.hyuabot.service.database.entity.Event
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: AppDatabase
): ViewModel() {
    private val _events = database.calendarDao().getAll().asLiveData()
    private val _updating = MutableLiveData(false)
    private val _queryError = MutableLiveData<QueryError?>(null)

    val updating get() = _updating
    val events get() = _events
    val queryError get() = _queryError

    fun fetchCalendarVersion() {
        _updating.postValue(true)
        viewModelScope.launch {
            val response = apolloClient.query(CalendarPageVersionQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.calendar != null) {
                val localVersion = userPreferencesRepository.calendarVersion.first()
                if (localVersion != response.data?.calendar?.version || database.calendarDao().count() == 0) {
                    fetchEvents()
                }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _updating.postValue(false)
        }
    }

    private fun fetchEvents() {
        val dao = database.calendarDao()
        viewModelScope.launch {
            val response = apolloClient.query(CalendarPageQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.calendar != null) {
                dao.deleteAll()
                response.data?.calendar?.let { calendar ->
                    val events = calendar.categories.flatMap { category ->
                        category.events.map {
                            Event(
                                eventID = it.seq,
                                title = it.title,
                                description = it.description,
                                startDate = it.start.toString(),
                                endDate = it.end.toString(),
                                category = category.name
                            )
                        }
                    }
                    dao.insertAll(*events.toTypedArray())
                    calendar.version.let { version -> userPreferencesRepository.setCalendarVersion(version) }
                    translateEventsInCache(events)
                }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _updating.postValue(false)
        }
    }

    private fun translateEventsInCache(events: List<Event>) {
        viewModelScope.launch {
            val translatedEvents = events.map {
                it.copy(
                    title = DynamicTextTranslator.translateForCurrentAppLocale(it.title),
                    description = DynamicTextTranslator.translateForCurrentAppLocale(it.description),
                )
            }
            database.calendarDao().insertAll(*translatedEvents.toTypedArray())
        }
    }
}
