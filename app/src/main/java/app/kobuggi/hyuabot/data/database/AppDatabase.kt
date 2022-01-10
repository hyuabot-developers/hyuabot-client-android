package app.kobuggi.hyuabot.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.kobuggi.hyuabot.data.database.AppDatabase.Companion.DB_VERSION
import app.kobuggi.hyuabot.data.database.dao.PhoneNumberDao
import app.kobuggi.hyuabot.data.database.entity.PhoneNumberEntity

@Database(entities = [PhoneNumberEntity::class], version = DB_VERSION, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getPhoneNumberDao(): PhoneNumberDao

    companion object{
        const val DB_VERSION = 1
        private const val DB_NAME = "app.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this){
            instance ?: build(context).also{ instance = it }
        }

        private fun build(context: Context) = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
            .build()
    }
}