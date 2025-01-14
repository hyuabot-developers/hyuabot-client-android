package app.kobuggi.hyuabot.ui.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.MapPageQuery
import app.kobuggi.hyuabot.MapPageSearchQuery
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val apolloClient: ApolloClient) : ViewModel() {
    private val _buildings = MutableLiveData<List<MapPageQuery.Building>>()
    private val _rooms = MutableLiveData<List<MapPageSearchQuery.Room>>()
    private val _queryError = MutableLiveData<QueryError?>(null)

    val buildings get() = _buildings
    val rooms get() = _rooms
    val searchRooms = MutableLiveData(false)
    val queryError get() = _queryError

    fun fetchBuildings(north: Double?, south: Double?, west: Double?, east: Double?) {
        viewModelScope.launch {
            val response = apolloClient.query(MapPageQuery(north ?: 0.0, south ?: 0.0, west ?: 0.0, east ?: 0.0)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.building != null) {
                _buildings.value = response.data?.building ?: emptyList()
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
        }
    }

    fun searchRooms(query: String) {
        viewModelScope.launch {
            val response = apolloClient.query(MapPageSearchQuery(query)).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.room != null) {
                _rooms.value = response.data?.room ?: emptyList()
                searchRooms.value = true
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
        }
    }
}
