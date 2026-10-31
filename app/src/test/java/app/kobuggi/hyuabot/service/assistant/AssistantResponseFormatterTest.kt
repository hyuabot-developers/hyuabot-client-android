package app.kobuggi.hyuabot.service.assistant

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.service.time.MealPeriod
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35], qualifiers = "ko")
class AssistantResponseFormatterTest {
    private lateinit var context: Context
    private lateinit var formatter: AssistantResponseFormatter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        formatter = AssistantResponseFormatter(context)
    }

    @Test
    fun formatsShuttleDepartureFollowingAndNow() {
        val departure = NextShuttleAnswer.Departure(
            "dormitory_o", "STATION", LocalTime.of(10, 25), 25, LocalTime.of(10, 40),
        )
        assertEquals(
            "기숙사에서 25분 후(10:25) 출발, 행선지 한대앞 다음 차는 10:40 출발이에요.",
            formatter.format(departure),
        )
        assertEquals(
            context.getString(
                R.string.assistant_shuttle_next_now,
                formatter.stopName("dormitory_o"), "10:25", formatter.destinationName("STATION"),
            ),
            formatter.format(departure.copy(minutesUntil = 0, followingTime = null)),
        )
    }

    @Test
    fun formatsNoServiceAndNetworkErrors() {
        assertEquals(
            "한대앞 셔틀 운행이 종료됐어요.",
            formatter.format(NextShuttleAnswer.NoMoreService("station", null)),
        )
        assertEquals(context.getString(R.string.assistant_network_error), formatter.format(NextShuttleAnswer.NetworkError))
        assertEquals(context.getString(R.string.assistant_network_error), formatter.format(CafeteriaMenuAnswer.NetworkError))
    }

    @Test
    fun formatsCafeteriaAnswers() {
        val menu = CafeteriaMenuAnswer.Menus(
            LocalDate.of(2026, 10, 31),
            MealPeriod.LUNCH,
            listOf(CafeteriaMealSummary(12, "11:00-14:00", listOf("김치찌개", "계란말이"))),
        )
        val entry = context.getString(
            R.string.assistant_cafeteria_entry_with_time,
            formatter.cafeteriaName(12), "11:00-14:00", "김치찌개, 계란말이",
        )
        assertEquals(context.getString(R.string.assistant_cafeteria_menus, formatter.mealLabel(MealPeriod.LUNCH), entry), formatter.format(menu))
        assertEquals(
            context.getString(R.string.assistant_cafeteria_no_menu, formatter.mealLabel(MealPeriod.LUNCH)),
            formatter.format(CafeteriaMenuAnswer.NoMenu(LocalDate.of(2026, 10, 31), MealPeriod.LUNCH, null)),
        )
        assertEquals(
            context.getString(R.string.assistant_cafeteria_unknown, "없는식당"),
            formatter.format(CafeteriaMenuAnswer.UnknownCafeteria("없는식당")),
        )
    }

    @Test
    fun mapsResponsesToAppFunctionModels() {
        val shuttle = AssistantResponses.shuttle(
            NextShuttleAnswer.Departure("dormitory_o", "STATION", LocalTime.of(10, 25), 25, LocalTime.of(10, 40)),
            formatter,
        )
        assertEquals(AssistantResponses.STATUS_OK, shuttle.status)
        assertEquals(25, shuttle.minutesUntil)

        val cafeteria = AssistantResponses.cafeteria(
            CafeteriaMenuAnswer.Menus(
                LocalDate.of(2026, 10, 31), MealPeriod.LUNCH,
                listOf(CafeteriaMealSummary(12, "11:00-14:00", listOf("김치찌개"))),
            ),
            formatter,
        )
        assertEquals(context.getString(R.string.widget_cafeteria_12), cafeteria.cafeterias[0].name)
    }
}
