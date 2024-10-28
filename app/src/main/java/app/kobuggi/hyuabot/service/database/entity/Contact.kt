package app.kobuggi.hyuabot.service.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact (
    @PrimaryKey @ColumnInfo(name = "id") val contactID: Int,
    @ColumnInfo(name = "campusID") val campusID: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "phone") val phone: String,
)
