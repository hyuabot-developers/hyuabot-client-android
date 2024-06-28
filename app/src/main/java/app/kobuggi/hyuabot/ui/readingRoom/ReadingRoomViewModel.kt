package app.kobuggi.hyuabot.ui.readingRoom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ReadingRoomPageQuery
import app.kobuggi.hyuabot.databinding.FragmentReadingRoomBinding
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import com.apollographql.apollo3.ApolloClient
import com.google.android.material.snackbar.Snackbar
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
    val notificationList = userPreferencesRepository.readingRoomNotifications.asLiveData()


    val rooms: MutableLiveData<List<ReadingRoomPageQuery.ReadingRoom>>
        get() = _rooms

    fun fetchRooms() {
        viewModelScope.launch {
            val response = try {
                apolloClient.query(ReadingRoomPageQuery(campus = 2)).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            _rooms.value = response?.data?.readingRoom ?: emptyList()
        }
    }

    fun toggleReadingRoomNotification(binding: FragmentReadingRoomBinding, readingRoomID: Int, subscribe: Boolean) {
        if (subscribe) {
            Firebase.messaging.subscribeToTopic("reading_room_$readingRoomID").addOnSuccessListener {
                Snackbar.make(binding.root, R.string.reading_room_noti_on, Snackbar.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Snackbar.make(binding.root, R.string.reading_room_noti_on_error, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            Firebase.messaging.unsubscribeFromTopic("reading_room_$readingRoomID").addOnSuccessListener {
                Snackbar.make(binding.root, R.string.reading_room_noti_off, Snackbar.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Snackbar.make(binding.root, R.string.reading_room_noti_off_error, Snackbar.LENGTH_SHORT).show()
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.toggleReadingRoomNotification(readingRoomID)
        }
    }
}
