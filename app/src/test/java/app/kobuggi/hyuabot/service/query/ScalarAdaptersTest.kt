package app.kobuggi.hyuabot.service.query

import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.json.MapJsonReader
import com.apollographql.apollo.api.json.MapJsonWriter
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ScalarAdaptersTest {
    @Test
    fun localDateAdapterReadsAndWritesIsoDate() {
        val value = LocalDateAdapter.fromJson(MapJsonReader("2026-06-24"), CustomScalarAdapters.Empty)
        val writer = MapJsonWriter()

        LocalDateAdapter.toJson(writer, CustomScalarAdapters.Empty, value)

        assertEquals(LocalDate.of(2026, 6, 24), value)
        assertEquals("2026-06-24", writer.root())
    }

    @Test
    fun localTimeAdapterReadsAndWritesIsoTime() {
        val value = LocalTimeAdapter.fromJson(MapJsonReader("09:30:15"), CustomScalarAdapters.Empty)
        val writer = MapJsonWriter()

        LocalTimeAdapter.toJson(writer, CustomScalarAdapters.Empty, value)

        assertEquals(LocalTime.of(9, 30, 15), value)
        assertEquals("09:30:15", writer.root())
    }

    @Test
    fun zonedDateTimeAdapterReadsAndWritesIsoDateTime() {
        val value = ZonedDateTimeAdapter.fromJson(
            MapJsonReader("2026-06-24T12:30:00+09:00[Asia/Seoul]"),
            CustomScalarAdapters.Empty,
        )
        val writer = MapJsonWriter()

        ZonedDateTimeAdapter.toJson(writer, CustomScalarAdapters.Empty, value)

        assertEquals(ZonedDateTime.parse("2026-06-24T12:30:00+09:00[Asia/Seoul]"), value)
        assertEquals("2026-06-24T12:30+09:00[Asia/Seoul]", writer.root())
    }
}
