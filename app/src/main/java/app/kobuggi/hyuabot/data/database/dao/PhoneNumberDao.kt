package app.kobuggi.hyuabot.data.database.dao

import androidx.room.Query
import app.kobuggi.hyuabot.model.DatabaseItem

interface PhoneNumberDao {
    @Query("SELECT * FROM app where category = :category")
    fun getMarkersByCategory(category: String) : Array<DatabaseItem>

    @Query("SELECT * FROM app where name like :name and phone is not null")
    fun getPhoneNumberByName(name: String) : Array<DatabaseItem>

    @Query("SELECT * FROM app where category = :category and phone is not null")
    fun getPhoneNumberByCategory(category: String) : Array<DatabaseItem>

    @Query("SELECT * FROM app where phone is not null")
    fun getPhoneNumberAll() : Array<DatabaseItem>

    @Query("SELECT * FROM app where (category=\"korean\" or category=\"japanese\" or category=\"chinese\" or category=\"western\" or category=\"fast food\" or category=\"chicken\" or category=\"pizza\" or category=\"meat\" or category=\"other food\") and phone is not null")
    fun getPhoneNumberAllRestaurant() : Array<DatabaseItem>
}