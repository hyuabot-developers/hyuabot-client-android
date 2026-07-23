package app.kobuggi.hyuabot.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZonedDateTime

class HomeWeatherDisplayLogicTest {
    @Test
    fun `upcoming precipitation takes priority in the weather title`() {
        val now = ZonedDateTime.parse("2026-07-21T14:35:00+09:00")

        assertEquals(
            HomeWeatherTitleStyle.RAIN_LATER,
            HomeWeatherDisplayLogic.titleStyle(
                condition = "RAIN",
                currentTemperature = 29.0,
                maximumTemperature = 31.0,
                precipitationType = "RAIN",
                precipitationStartAt = ZonedDateTime.parse("2026-07-21T16:00:00+09:00"),
                now = now,
            ),
        )
        assertEquals(
            HomeWeatherTitleStyle.SNOW_NOW,
            HomeWeatherDisplayLogic.titleStyle(
                condition = "SNOW",
                currentTemperature = -3.0,
                maximumTemperature = 1.0,
                precipitationType = "SNOW",
                precipitationStartAt = ZonedDateTime.parse("2026-07-21T14:00:00+09:00"),
                now = now,
            ),
        )
        assertEquals(
            HomeWeatherTitleStyle.SLEET_TODAY,
            HomeWeatherDisplayLogic.titleStyle(
                condition = "SLEET",
                currentTemperature = 1.0,
                maximumTemperature = 3.0,
                precipitationType = "SLEET",
                precipitationStartAt = null,
                now = now,
            ),
        )
    }

    @Test
    fun `weather title falls back to temperature and sky condition`() {
        assertEquals(
            HomeWeatherTitleStyle.HOT,
            style(condition = "CLEAR", current = 32.0, maximum = 35.0),
        )
        assertEquals(
            HomeWeatherTitleStyle.COLD,
            style(condition = "CLEAR", current = -6.0, maximum = 1.0),
        )
        assertEquals(
            HomeWeatherTitleStyle.CLEAR,
            style(condition = "CLEAR", current = 20.0, maximum = 25.0),
        )
        assertEquals(
            HomeWeatherTitleStyle.CLOUDY,
            style(condition = "CLOUDY", current = 20.0, maximum = 25.0),
        )
    }

    private fun style(
        condition: String,
        current: Double,
        maximum: Double,
    ) = HomeWeatherDisplayLogic.titleStyle(
        condition = condition,
        currentTemperature = current,
        maximumTemperature = maximum,
        precipitationType = "NONE",
        precipitationStartAt = null,
    )
}
