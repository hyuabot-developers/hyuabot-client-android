package app.kobuggi.hyuabot.service.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Event (
    @PrimaryKey @ColumnInfo(name = "id") val eventID: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "startDate") val startDate: String,
    @ColumnInfo(name = "endDate") val endDate: String,
)
