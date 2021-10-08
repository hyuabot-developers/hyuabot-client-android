package app.kobuggi.hyuabot.function

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import app.kobuggi.hyuabot.model.DatabaseItem

@Database(entities = [DatabaseItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseHelper(): DatabaseHelper?
}