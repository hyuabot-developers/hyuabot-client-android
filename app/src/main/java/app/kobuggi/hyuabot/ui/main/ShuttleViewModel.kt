package app.kobuggi.hyuabot.ui.main

import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.core.BaseViewModel
import app.kobuggi.hyuabot.data.remote.api.ApplicationAPI
import app.kobuggi.hyuabot.model.ShuttleDataItem
import app.kobuggi.hyuabot.util.NotNullMutableLiveData

class ShuttleViewModel(private val api: ApplicationAPI) : BaseViewModel() {
    private val _refreshing: NotNullMutableLiveData<Boolean> = NotNullMutableLiveData(false)
    val refreshing: NotNullMutableLiveData<Boolean> get() = _refreshing

    private val _shuttleCardItems: NotNullMutableLiveData<List<ShuttleDataItem>> = NotNullMutableLiveData(arrayListOf())
    val shuttleCardItems: NotNullMutableLiveData<List<ShuttleDataItem>> get() = _shuttleCardItems

    fun fetchShuttleArrival(){
        addToDisposable(api.getShuttleArrivalInfo()
            .doOnSubscribe { _refreshing.value = true }
            .doOnSuccess { _refreshing.value = false }
            .doOnError { _refreshing.value = false }
            .subscribe({
                _shuttleCardItems.value = listOf(
                    ShuttleDataItem(R.string.dorm_to_station, it.Residence.forStation),
                    ShuttleDataItem(R.string.dorm_to_terminal, it.Residence.forTerminal),
                    ShuttleDataItem(R.string.shuttlecock_to_station, it.Shuttlecock_O.forStation),
                    ShuttleDataItem(R.string.shuttlecock_to_terminal, it.Shuttlecock_O.forTerminal),
                    ShuttleDataItem(R.string.station, it.Subway.forStation),
                    ShuttleDataItem(R.string.terminal, it.Terminal.forTerminal),
                    ShuttleDataItem(R.string.shuttlecock_i, it.Shuttlecock_I.forTerminal)
                )
            }, {
                // handle errors
            })
        )
    }
}