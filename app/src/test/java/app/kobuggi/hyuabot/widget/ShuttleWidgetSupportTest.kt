package app.kobuggi.hyuabot.widget

import android.app.Application
import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class ShuttleWidgetSupportTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun distanceToReturnsSquaredCoordinateDistance() {
        val stop = ShuttleWidgetQuery.Stop(
            __typename = "ShuttleStop",
            latitude = 2.0,
            longitude = 5.0,
            name = "station",
            timetable = timetable(),
        )
        val location = Location("test").apply {
            latitude = -1.0
            longitude = 1.0
        }

        assertEquals(25.0, ShuttleWidgetSupport.distanceTo(stop, location), 0.0)
    }

    @Test
    fun makeGroupsFormatsFirstFiveTimesAndDropsEmptyDestinations() {
        val groups = ShuttleWidgetSupport.makeGroups(
            context,
            timetable(
                destination("STATION", 8, 9, 10, 11, 12, 13),
                destination("UNKNOWN", 7),
                destination("TERMINAL"),
            ),
        )

        assertEquals(
            listOf(
                ShuttleGroup(
                    context.getString(R.string.shuttle_bound_for_station),
                    listOf("08:00", "09:00", "10:00", "11:00", "12:00"),
                ),
                ShuttleGroup("UNKNOWN", listOf("07:00")),
            ),
            groups,
        )
    }

    @Test
    fun stopDisplayNameMapsKnownStopCodesAndKeepsUnknownCode() {
        assertEquals(
            context.getString(R.string.shuttle_tab_dormitory_out),
            ShuttleWidgetSupport.stopDisplayName(context, "dormitory_o"),
        )
        assertEquals(
            context.getString(R.string.shuttle_tab_dormitory_out),
            ShuttleWidgetSupport.stopDisplayName(context, "dormitory_i"),
        )
        assertEquals(
            context.getString(R.string.shuttle_tab_shuttlecock_out),
            ShuttleWidgetSupport.stopDisplayName(context, "shuttlecock_o"),
        )
        assertEquals(
            context.getString(R.string.shuttle_tab_station),
            ShuttleWidgetSupport.stopDisplayName(context, "station"),
        )
        assertEquals(
            context.getString(R.string.shuttle_tab_terminal),
            ShuttleWidgetSupport.stopDisplayName(context, "terminal"),
        )
        assertEquals(
            context.getString(R.string.shuttle_tab_jungang_station),
            ShuttleWidgetSupport.stopDisplayName(context, "jungang_stn"),
        )
        assertEquals(
            context.getString(R.string.shuttle_tab_shuttlecock_in),
            ShuttleWidgetSupport.stopDisplayName(context, "shuttlecock_i"),
        )
        assertEquals("unknown", ShuttleWidgetSupport.stopDisplayName(context, "unknown"))
    }

    @Test
    fun destinationDisplayNameMapsKnownDestinationCodesAndKeepsUnknownCode() {
        assertEquals(
            context.getString(R.string.shuttle_bound_for_station),
            ShuttleWidgetSupport.destinationDisplayName(context, "STATION"),
        )
        assertEquals(
            context.getString(R.string.shuttle_bound_for_terminal),
            ShuttleWidgetSupport.destinationDisplayName(context, "TERMINAL"),
        )
        assertEquals(
            context.getString(R.string.shuttle_bound_for_jungang_station),
            ShuttleWidgetSupport.destinationDisplayName(context, "JUNGANG"),
        )
        assertEquals(
            context.getString(R.string.shuttle_bound_for_dormitory),
            ShuttleWidgetSupport.destinationDisplayName(context, "CAMPUS"),
        )
        assertEquals("UNKNOWN", ShuttleWidgetSupport.destinationDisplayName(context, "UNKNOWN"))
    }

    private fun timetable(
        vararg destinations: ShuttleWidgetQuery.Destination,
    ): ShuttleWidgetQuery.Timetable =
        ShuttleWidgetQuery.Timetable(
            __typename = "ShuttleTimetable",
            destination = destinations.toList(),
        )

    private fun destination(
        code: String,
        vararg hours: Int,
    ): ShuttleWidgetQuery.Destination =
        ShuttleWidgetQuery.Destination(
            __typename = "ShuttleDestination",
            destination = code,
            entries = hours.map {
                ShuttleWidgetQuery.Entry(
                    __typename = "ShuttleTimetableEntry",
                    time = LocalTime.of(it, 0),
                )
            },
        )
}
