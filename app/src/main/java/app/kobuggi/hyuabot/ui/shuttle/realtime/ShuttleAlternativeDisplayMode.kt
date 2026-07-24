package app.kobuggi.hyuabot.ui.shuttle.realtime

import androidx.annotation.StringRes
import app.kobuggi.hyuabot.R

enum class ShuttleAlternativeDisplayMode(
    val value: String,
    @param:StringRes val titleRes: Int,
) {
    AUTOMATIC("automatic", R.string.shuttle_quick_settings_alternative_mode_automatic),
    ALWAYS("always", R.string.shuttle_quick_settings_alternative_mode_always),
    HIDDEN("hidden", R.string.shuttle_quick_settings_alternative_mode_hidden),
    ;

    companion object {
        fun from(value: String?): ShuttleAlternativeDisplayMode =
            entries.firstOrNull { it.value == value } ?: AUTOMATIC
    }
}
