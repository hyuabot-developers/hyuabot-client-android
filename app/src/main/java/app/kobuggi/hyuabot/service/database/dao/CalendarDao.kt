package app.kobuggi.hyuabot.service.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.kobuggi.hyuabot.service.database.entity.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM event ORDER BY startDate ASC")
    fun getAll(): Flow<List<Event>>

    @Insert
    suspend fun insertAll(vararg events: Event)

    @Query("DELETE FROM event")
    suspend fun deleteAll()
}
