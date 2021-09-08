package app.kobuggi.hyuabot.function

import androidx.room.Dao
import androidx.room.Query
import app.kobuggi.hyuabot.model.DatabaseItem

@Dao
interface DatabaseHelper {
    @Query("SELECT * FROM app where category = :category")
    fun getMarkersByCategory(category: String) : Array<DatabaseItem>
}