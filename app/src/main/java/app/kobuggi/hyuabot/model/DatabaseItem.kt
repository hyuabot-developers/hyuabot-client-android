package app.kobuggi.hyuabot.model

import androidx.room.*

@Entity(tableName = "app")
data class DatabaseItem(
    @PrimaryKey @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "category") val category : String,
    @ColumnInfo(name = "phone") val phoneNumber : String?,
    @ColumnInfo(name = "latitude") val latitude : Double?,
    @ColumnInfo(name = "longitude") val longitude : Double?,
    @ColumnInfo(name = "description") val description : String?
)
