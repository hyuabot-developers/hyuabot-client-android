package app.kobuggi.hyuabot.util

import android.content.res.Configuration
import android.content.res.Resources

object UIUtility {
    fun isDarkModeOn(resources: Resources): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}
