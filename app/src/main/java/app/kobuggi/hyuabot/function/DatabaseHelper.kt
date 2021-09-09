package app.kobuggi.hyuabot.function

import androidx.room.Dao
import androidx.room.Query
import app.kobuggi.hyuabot.model.DatabaseItem

@Dao
interface DatabaseHelper {
    @Query("SELECT * FROM app where category = :category")
    fun getMarkersByCategory(category: String) : Array<DatabaseItem>

    @Query("SELECT * FROM app where category = :category and name like :keyword and phone is not null")
    fun getPhoneNumber(category: String, keyword: String) : Array<DatabaseItem>
}