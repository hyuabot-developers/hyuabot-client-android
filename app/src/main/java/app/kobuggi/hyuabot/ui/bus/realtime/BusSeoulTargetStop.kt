package app.kobuggi.hyuabot.ui.bus.realtime

import app.kobuggi.hyuabot.R

enum class BusSeoulTargetStop(val value: String, val titleRes: Int, val stopID: Int) {
    SEOCHO("seocho", R.string.bus_stop_seocho, 121000060),
    GYODAE("gyodae", R.string.bus_stop_gyodae, 121000929),
    GANGNAM("gangnam", R.string.bus_stop_gangnam, 121000974),
    YANGJAE("yangjae", R.string.bus_stop_yangjae, 121000970),
    YANGJAE_CITIZENS_FOREST("yangjae_forest", R.string.bus_stop_yangjae_forest, 121000220);

    companion object {
        fun from(value: String?): BusSeoulTargetStop {
            return entries.firstOrNull { it.value == value } ?: GANGNAM
        }
    }
}
