package app.kobuggi.hyuabot.ui.home

import java.time.ZoneId
import java.time.ZonedDateTime

enum class HomeWeatherTitleStyle {
    CLEAR,
    CLOUDY,
    HOT,
    COLD,
    RAIN_NOW,
    RAIN_LATER,
    RAIN_TODAY,
    SLEET_NOW,
    SLEET_LATER,
    SLEET_TODAY,
    SNOW_NOW,
    SNOW_LATER,
    SNOW_TODAY,
}

object HomeWeatherDisplayLogic {
    private val serviceZone = ZoneId.of("Asia/Seoul")

    fun titleStyle(
        condition: String,
        currentTemperature: Double?,
        maximumTemperature: Double?,
        precipitationType: String,
        precipitationStartAt: ZonedDateTime?,
        now: ZonedDateTime = ZonedDateTime.now(serviceZone),
    ): HomeWeatherTitleStyle {
        val precipitationStyles = when (precipitationType) {
            "RAIN" -> Triple(
                HomeWeatherTitleStyle.RAIN_NOW,
                HomeWeatherTitleStyle.RAIN_LATER,
                HomeWeatherTitleStyle.RAIN_TODAY,
            )
            "SLEET" -> Triple(
                HomeWeatherTitleStyle.SLEET_NOW,
                HomeWeatherTitleStyle.SLEET_LATER,
                HomeWeatherTitleStyle.SLEET_TODAY,
            )
            "SNOW" -> Triple(
                HomeWeatherTitleStyle.SNOW_NOW,
                HomeWeatherTitleStyle.SNOW_LATER,
                HomeWeatherTitleStyle.SNOW_TODAY,
            )
            else -> null
        }
        if (precipitationStyles != null) {
            return when {
                precipitationStartAt == null -> precipitationStyles.third
                precipitationStartAt.toInstant() <= now.toInstant() -> precipitationStyles.first
                else -> precipitationStyles.second
            }
        }
        if (maximumTemperature != null && maximumTemperature >= 32.0) {
            return HomeWeatherTitleStyle.HOT
        }
        if (currentTemperature != null && currentTemperature <= -5.0) {
            return HomeWeatherTitleStyle.COLD
        }
        return if (condition == "CLEAR") {
            HomeWeatherTitleStyle.CLEAR
        } else {
            HomeWeatherTitleStyle.CLOUDY
        }
    }
}
