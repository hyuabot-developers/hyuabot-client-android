package app.kobuggi.hyuabot.ui.home

import androidx.annotation.StringRes
import app.kobuggi.hyuabot.R

enum class BusHomeDestination(
    val value: String,
    @StringRes val titleRes: Int,
) {
    SANGNOKSU("sangnoksu", R.string.home_bus_destination_sangnoksu),
    GANGNAM("gangnam", R.string.home_bus_destination_gangnam),
    SUWON("suwon", R.string.home_bus_destination_suwon),
    UIWANG("uiwang", R.string.home_bus_destination_uiwang),
    GUNPO("gunpo", R.string.home_bus_destination_gunpo),
}
