package app.kobuggi.hyuabot.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app")
data class PhoneNumberEntity(
    @PrimaryKey @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "category") val category : String,
    @ColumnInfo(name = "phone") val phoneNumber : String?,
    @ColumnInfo(name = "latitude") val latitude : Double?,
    @ColumnInfo(name = "longitude") val longitude : Double?,
    @ColumnInfo(name = "description") val description : String?
)
