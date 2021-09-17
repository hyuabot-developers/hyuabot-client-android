package app.kobuggi.hyuabot.function

import androidx.room.Dao
import androidx.room.Query
import app.kobuggi.hyuabot.model.DatabaseItem

@Dao
interface DatabaseHelper {
    @Query("SELECT * FROM app where category = :category")
    fun getMarkersByCategory(category: String) : Array<DatabaseItem>

    @Query("SELECT * FROM app where category = :category and phone is not null")
    fun getPhoneNumberByCategory(category: String) : Array<DatabaseItem>

    @Query("SELECT * FROM app where phone is not null")
    fun getPhoneNumberAll() : Array<DatabaseItem>

    @Query("SELECT * FROM app where (category=\"korean\" or category=\"japanese\" or category=\"chinese\" or category=\"western\" or category=\"fast food\" or category=\"chicken\" or category=\"pizza\" or category=\"meat\" or category=\"other food\") and phone is not null")
    fun getPhoneNumberAllRestaurant() : Array<DatabaseItem>
}