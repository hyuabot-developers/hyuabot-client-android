package app.kobuggi.hyuabot.ui.home

import app.kobuggi.hyuabot.R

enum class HomeSubwayTransferDestination(val value: String, val titleRes: Int) {
    SEOUL("seoul", R.string.home_quick_settings_subway_destination_seoul),
    SUWON_YONGIN("suwon_yongin", R.string.home_quick_settings_subway_destination_suwon_yongin),
    INCHEON("incheon", R.string.home_quick_settings_subway_destination_incheon),
    OIDO("oido", R.string.home_quick_settings_subway_destination_oido);

    companion object {
        fun from(value: String?): HomeSubwayTransferDestination {
            return entries.firstOrNull { it.value == value || it.debugValue == value } ?: SEOUL
        }
    }

    val debugValue: String
        get() = when (this) {
            SEOUL -> "seoul"
            SUWON_YONGIN -> "suwonYongin"
            INCHEON -> "incheon"
            OIDO -> "oido"
        }
}
