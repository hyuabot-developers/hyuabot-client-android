package app.kobuggi.hyuabot.ui.main

import app.kobuggi.hyuabot.core.BaseViewModel
import app.kobuggi.hyuabot.data.remote.api.ApplicationAPI
import app.kobuggi.hyuabot.data.remote.domain.restaurant.RestaurantItem
import app.kobuggi.hyuabot.util.NotNullMutableLiveData

class RestaurantMenuViewModel(private val api: ApplicationAPI) : BaseViewModel() {
    private val _refreshing: NotNullMutableLiveData<Boolean> = NotNullMutableLiveData(false)
    val refreshing: NotNullMutableLiveData<Boolean> get() = _refreshing

    private val _restaurantCardItems: NotNullMutableLiveData<List<RestaurantItem>> = NotNullMutableLiveData(arrayListOf())
    val restaurantCardItems: NotNullMutableLiveData<List<RestaurantItem>> get() = _restaurantCardItems

    fun fetchRestaurantMenu(){
        val params = mutableMapOf<String, String>().apply {
            this["campus"] = "erica"
        }

        addToDisposable(api.getRestaurantMenu(params)
            .doOnSubscribe { _refreshing.value = true }
            .doOnSuccess { _refreshing.value = false }
            .doOnError { _refreshing.value = false }
            .subscribe({
                _restaurantCardItems.value = it.toList()
            }, {
                // handle errors
            })
        )
    }
}