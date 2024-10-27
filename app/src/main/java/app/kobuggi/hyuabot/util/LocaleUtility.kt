package app.kobuggi.hyuabot.util

import android.content.Context
import java.util.Locale

class LocaleUtility {
    companion object {
        var locale: Locale? = null

        fun wrap(context: Context): Context {
            if(locale == null){
                return context
            }

            val resource = context.resources
            val configuration = resource.configuration
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        }

        fun setLocale(localeCode: String){
            locale = Locale(localeCode)
        }
    }
}
