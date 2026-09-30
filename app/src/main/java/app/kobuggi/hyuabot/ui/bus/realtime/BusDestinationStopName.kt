package app.kobuggi.hyuabot.ui.bus.realtime

import androidx.annotation.StringRes
import app.kobuggi.hyuabot.R

@StringRes
fun busDestinationStopNameResource(stopID: Int?): Int? = when (stopID) {
    216000138 -> R.string.bus_stop_sangnoksu_station
    216000378 -> R.string.stop_name_hanyang_erica_convention_center
    216000048 -> R.string.stop_name_hanyang_university
    216000141 -> R.string.stop_name_hanyang_university_gate
    202000208 -> R.string.stop_name_suwon_station
    216000117 -> R.string.stop_name_seongpo_residential_area
    226000042 -> R.string.stop_name_uiwang_city_hall
    225000116 -> R.string.stop_name_gunpo_city_hall
    213000487 -> R.string.bus_stop_gwangmyeong_station
    121000060 -> R.string.bus_stop_seocho
    121000929 -> R.string.bus_stop_gyodae
    121000974 -> R.string.bus_stop_gangnam
    121000970 -> R.string.bus_stop_yangjae
    121000220 -> R.string.bus_stop_yangjae_forest
    else -> null
}
