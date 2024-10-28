package app.kobuggi.hyuabot.service.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.kobuggi.hyuabot.service.database.entity.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact ORDER BY name ASC")
    fun getAll(): Flow<List<Contact>>

    @Query("SELECT * FROM contact WHERE campusID = :campusID")
    fun findByCampusID(campusID: Int): Flow<List<Contact>>

    @Query("SELECT * FROM contact WHERE name LIKE :name")
    fun findByName(name: String): Flow<List<Contact>>

    @Query("SELECT * FROM contact WHERE name LIKE :name AND campusID = :campusID")
    fun findByNameAndCampusID(name: String, campusID: Int): Flow<List<Contact>>

    @Insert
    suspend fun insertAll(vararg contacts: Contact)

    @Query("DELETE FROM contact")
    suspend fun deleteAll()
}
