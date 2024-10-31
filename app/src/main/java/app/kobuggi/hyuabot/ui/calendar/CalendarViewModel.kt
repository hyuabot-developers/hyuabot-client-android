package app.kobuggi.hyuabot.ui.calendar

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.CalendarPageQuery
import app.kobuggi.hyuabot.CalendarPageVersionQuery
import app.kobuggi.hyuabot.service.database.AppDatabase
import app.kobuggi.hyuabot.service.database.entity.Event
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: AppDatabase
): ViewModel() {
    private val calendarVersion = userPreferencesRepository.calendarVersion.asLiveData()
    private val _events = database.calendarDao().getAll().asLiveData()
    private val _updating = MutableLiveData(false)

    val updating get() = _updating
    val events get() = _events

    fun fetchCalendarVersion() {
        _updating.postValue(true)
        viewModelScope.launch {
            val response = try {
                apolloClient.query(CalendarPageVersionQuery()).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            calendarVersion.observeForever {
                if (it != response?.data?.calendar?.version) {
                    fetchEvents()
                } else {
                    _updating.postValue(false)
                }
            }
        }
    }

    private fun fetchEvents() {
        val dao = database.calendarDao()
        viewModelScope.launch {
            val response = try {
                apolloClient.query(CalendarPageQuery()).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            dao.deleteAll()
            response?.data?.calendar?.version?.let { userPreferencesRepository.setCalendarVersion(it) }
            response?.data?.calendar?.data?.map {
                Event(
                    eventID = it.id,
                    title = it.title,
                    description = it.description,
                    startDate = it.start.toString(),
                    endDate = it.end.toString(),
                    category = it.category.name
                )
            }?.let { dao.insertAll(*it.toTypedArray()) }
            _updating.postValue(false)
        }
    }
}
