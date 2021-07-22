package app.kobuggi.hyuabot.function

import android.content.res.Configuration

fun getDarkMode(configuration : Configuration): Boolean {
    return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}