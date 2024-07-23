package app.kobuggi.hyuabot.service.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.kobuggi.hyuabot.service.database.dao.ContactDao
import app.kobuggi.hyuabot.service.database.entity.Contact

@Database(entities = [Contact::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
