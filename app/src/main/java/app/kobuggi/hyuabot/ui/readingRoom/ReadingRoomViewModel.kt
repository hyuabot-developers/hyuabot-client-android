package app.kobuggi.hyuabot.ui.readingRoom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ReadingRoomPageQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingRoomViewModel @Inject constructor(
    private val apolloClient: ApolloClient
): ViewModel() {
    private val _rooms = MutableLiveData<List<ReadingRoomPageQuery.ReadingRoom>>()

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
}
