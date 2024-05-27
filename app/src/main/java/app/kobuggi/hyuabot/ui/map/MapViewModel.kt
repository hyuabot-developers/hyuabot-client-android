package app.kobuggi.hyuabot.ui.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.MapPageQuery
import com.apollographql.apollo3.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val apolloClient: ApolloClient) : ViewModel() {
    private val _buildings = MutableLiveData<List<MapPageQuery.Building>>()

    val buildings get() = _buildings

    fun fetchBuildings(north: Double?, south: Double?, west: Double?, east: Double?) {
        viewModelScope.launch {
            val response = try {
                apolloClient.query(MapPageQuery(north ?: 0.0, south ?: 0.0, west ?: 0.0, east ?: 0.0)).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            _buildings.value = response?.data?.building ?: emptyList()
        }
    }
}
