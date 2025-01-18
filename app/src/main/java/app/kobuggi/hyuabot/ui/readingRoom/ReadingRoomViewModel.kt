package app.kobuggi.hyuabot.ui.readingRoom

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ReadingRoomPageQuery
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingRoomViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
    private val _rooms = MutableLiveData<List<ReadingRoomPageQuery.ReadingRoom>>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val notificationList = userPreferencesRepository.readingRoomNotifications.asLiveData()
    val extendNotificationTime = userPreferencesRepository.readingRoomExtendNotification.asLiveData()

    val campusID get() = userPreferencesRepository.campusID.asLiveData()
    val rooms: MutableLiveData<List<ReadingRoomPageQuery.ReadingRoom>>
        get() = _rooms
    val queryError: MutableLiveData<QueryError?>
        get() = _queryError

    fun fetchRooms(campusID: Int = 2) {
        viewModelScope.launch {
            val response = apolloClient.query(ReadingRoomPageQuery(campus = campusID)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.readingRoom != null) {
                _rooms.value = response.data?.readingRoom ?: emptyList()
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
        }
    }

    fun toggleReadingRoomNotification(context: Context, readingRoomID: Int, subscribe: Boolean) {
        if (subscribe) {
            Firebase.messaging.subscribeToTopic("reading_room_$readingRoomID").addOnSuccessListener {
                Toast.makeText(context, R.string.reading_room_noti_on, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, R.string.reading_room_noti_on_error, Toast.LENGTH_SHORT).show()
            }
        } else {
            Firebase.messaging.unsubscribeFromTopic("reading_room_$readingRoomID").addOnSuccessListener {
                Toast.makeText(context, R.string.reading_room_noti_off, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, R.string.reading_room_noti_off_error, Toast.LENGTH_SHORT).show()
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.toggleReadingRoomNotification(readingRoomID)
        }
    }

    fun setExtendNotificationTime(time: String?) {
        viewModelScope.launch {
            userPreferencesRepository.setReadingRoomExtendNotification(time)
        }
    }
}
