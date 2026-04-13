package app.kobuggi.hyuabot.service.query

import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import java.time.ZonedDateTime

object ZonedDateTimeAdapter: Adapter<ZonedDateTime> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): ZonedDateTime {
        return ZonedDateTime.parse(reader.nextString())
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: ZonedDateTime) {
        writer.value(value.toString())
    }
}
