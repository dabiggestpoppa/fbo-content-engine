package top.deeke.groupcontrol.repository

import kotlinx.coroutines.flow.Flow
import top.deeke.groupcontrol.database.dao.CommandDao
import top.deeke.groupcontrol.database.entity.CommandEntity

class CommandRepository(private val commandDao: CommandDao) {
    
    fun getAllCommands(): Flow<List<CommandEntity>> = commandDao.getAllCommands()
    
    suspend fun getCommandById(id: Int): CommandEntity? = commandDao.getCommandById(id)
    
    suspend fun insertCommand(command: CommandEntity): Long = commandDao.insertCommand(command)
    
    suspend fun insertCommands(commands: List<CommandEntity>) = commandDao.insertCommands(commands)
    
    suspend fun updateCommand(command: CommandEntity) = commandDao.updateCommand(command)
    
    suspend fun deleteCommand(command: CommandEntity) = commandDao.deleteCommand(command)
    
    suspend fun deleteAllCommands() = commandDao.deleteAllCommands()
    
    fun searchCommands(query: String): Flow<List<CommandEntity>> = commandDao.searchCommands(query)
}
