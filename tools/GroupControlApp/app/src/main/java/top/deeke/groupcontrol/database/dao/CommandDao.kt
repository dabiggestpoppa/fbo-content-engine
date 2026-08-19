package top.deeke.groupcontrol.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import top.deeke.groupcontrol.database.entity.CommandEntity

@Dao
interface CommandDao {
    @Query("SELECT * FROM commands ORDER BY isPinned DESC, `order` ASC, id ASC")
    fun getAllCommands(): Flow<List<CommandEntity>>
    
    @Query("SELECT * FROM commands WHERE id = :id")
    suspend fun getCommandById(id: Int): CommandEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: CommandEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommands(commands: List<CommandEntity>)
    
    @Update
    suspend fun updateCommand(command: CommandEntity)
    
    @Delete
    suspend fun deleteCommand(command: CommandEntity)
    
    @Query("DELETE FROM commands")
    suspend fun deleteAllCommands()
    
    @Query("SELECT * FROM commands WHERE title LIKE :query OR name LIKE :query OR description LIKE :query")
    fun searchCommands(query: String): Flow<List<CommandEntity>>
}
