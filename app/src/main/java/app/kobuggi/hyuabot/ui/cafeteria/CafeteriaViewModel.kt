package app.kobuggi.hyuabot.ui.cafeteria

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.model.cafeteria.RestaurantItemResponse
import app.kobuggi.hyuabot.service.rest.APIService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CafeteriaViewModel @Inject constructor(private val service: APIService) : ViewModel() {
    private val _campusID = MutableLiveData(2)
    private val _currentDate = MutableLiveData(LocalDate.now())
    private val _breakfastMenu = MutableLiveData<List<RestaurantItemResponse>>(listOf())
    private val _lunchMenu = MutableLiveData<List<RestaurantItemResponse>>(listOf())
    private val _dinnerMenu = MutableLiveData<List<RestaurantItemResponse>>(listOf())

    val currentDate get() = _currentDate
    val breakfast get() = _breakfastMenu
    val lunch get() = _lunchMenu
    val dinner get() = _dinnerMenu

    private fun setBreakfastMenu(menu: List<RestaurantItemResponse>) {
        _breakfastMenu.value = menu
    }

    private fun setLunchMenu(menu: List<RestaurantItemResponse>) {
        _lunchMenu.value = menu
    }

    private fun setDinnerMenu(menu: List<RestaurantItemResponse>) {
        _dinnerMenu.value = menu
    }

    private suspend fun fetchMenu(timeType: String) {
        val response = service.cafeteriaItem(_campusID.value!!, _currentDate.value!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), timeType)
        if (response.isSuccessful){
            when (timeType) {
                "조식" -> setBreakfastMenu(response.body()!!.restaurants)
                "중식" -> setLunchMenu(response.body()!!.restaurants)
                "석식" -> setDinnerMenu(response.body()!!.restaurants)
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            fetchMenu("조식")
            fetchMenu("중식")
            fetchMenu("석식")
        }
    }

    fun previousDate() {
        _currentDate.value = _currentDate.value!!.minusDays(1)
    }

    fun nextDate() {
        _currentDate.value = _currentDate.value!!.plusDays(1)
    }

    fun setCampusID(id: Int) {
        _campusID.value = id
    }
}