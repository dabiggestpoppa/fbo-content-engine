package top.deeke.groupcontrol.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.deeke.groupcontrol.database.AppDatabase
import top.deeke.groupcontrol.database.entity.CommandEntity
import top.deeke.groupcontrol.repository.CommandRepository
import top.deeke.groupcontrol.utils.DeekeScriptJsonParser
import java.io.BufferedReader
import java.io.InputStreamReader

class CommandViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val repository = CommandRepository(database.commandDao())
    private val parser = DeekeScriptJsonParser()
    
    private val _commands = MutableStateFlow<List<CommandEntity>>(emptyList())
    val commands: StateFlow<List<CommandEntity>> = _commands.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadCommands()
    }
    
    private fun loadCommands() {
        viewModelScope.launch {
            repository.getAllCommands().collect { commandList ->
                _commands.value = commandList
            }
        }
    }
    
    fun importDeekeScriptJson(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 读取文件内容
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        reader.readText()
                    }
                } ?: throw Exception("无法读取文件")
                
                // 解析JSON
                val methods = parser.parseDeekeScriptJson(jsonString)
                if (methods.isEmpty()) {
                    _errorMessage.value = "未找到有效的methods数据"
                    return@launch
                }
                
                // 转换为CommandEntity并保存到数据库
                val commands = methods.map { method ->
                    CommandEntity(
                        name = method.title,
                        title = method.title,
                        jsFile = method.jsFile,
                        description = "从deekeScript.json导入",
                        content = createMethodJsonString(method),
                        order = 0,
                        isPinned = false
                    )
                }
                
                repository.insertCommands(commands)
                
            } catch (e: Exception) {
                _errorMessage.value = "导入失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun deleteCommand(command: CommandEntity) {
        viewModelScope.launch {
            repository.deleteCommand(command)
        }
    }
    
    fun updateCommand(command: CommandEntity) {
        viewModelScope.launch {
            repository.updateCommand(command)
        }
    }

    fun deleteAllCommands() {
        viewModelScope.launch {
            repository.deleteAllCommands()
        }
    }
    
    private fun createMethodJsonString(method: DeekeScriptJsonParser.DeekeScriptMethod): String {
        val json = org.json.JSONObject()
        json.put("title", method.title)
        json.put("jsFile", method.jsFile)
        method.icon?.let { json.put("icon", it) }
        method.settingPage?.let { json.put("settingPage", it) }
        json.put("hidden", method.hidden)
        method.runType?.let { json.put("runType", it) }
        method.packageName?.let { json.put("packageName", it) }
        method.columns?.let { json.put("columns", it) }
        method.autoOpen?.let { json.put("autoOpen", it) }
        return json.toString()
    }
}
