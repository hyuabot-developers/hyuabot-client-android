package app.kobuggi.hyuabot.service.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.kobuggi.hyuabot.service.database.dao.CalendarDao
import app.kobuggi.hyuabot.service.database.dao.ContactDao
import app.kobuggi.hyuabot.service.database.entity.Contact
import app.kobuggi.hyuabot.service.database.entity.Event

@Database(entities = [Contact::class, Event::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun calendarDao(): CalendarDao
}
