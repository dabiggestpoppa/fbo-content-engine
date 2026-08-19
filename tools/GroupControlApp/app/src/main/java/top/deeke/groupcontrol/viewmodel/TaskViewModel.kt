package top.deeke.groupcontrol.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import top.deeke.groupcontrol.database.AppDatabase
import top.deeke.groupcontrol.database.entity.TaskEntity
import kotlinx.coroutines.launch
import top.deeke.groupcontrol.data.DataStoreManager
import top.deeke.groupcontrol.network.ApiService
import top.deeke.groupcontrol.repository.TaskRepository

class TaskViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val repository = TaskRepository(database.taskDao())
    private val dataStoreManager = DataStoreManager(context)
    private val apiService = ApiService()
    private val context = context

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.getAllTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun addTask(name: String, remark: String, commandId: Int, deviceIds: List<Int>, durationHours: Int, durationMinutes: Int) {
        viewModelScope.launch {
            try {
                val task = TaskEntity(
                    name = name,
                    remark = remark,
                    commandId = commandId,
                    deviceIds = deviceIds.joinToString(","),
                    durationHours = durationHours,
                    durationMinutes = durationMinutes,
                    status = "PENDING",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.insertTask(task)
            } catch (e: Exception) {
                _errorMessage.value = "添加任务失败: ${e.message}"
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                val updatedTask = task.copy(updatedAt = System.currentTimeMillis())
                repository.updateTask(updatedTask)
            } catch (e: Exception) {
                _errorMessage.value = "更新任务失败: ${e.message}"
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
            } catch (e: Exception) {
                _errorMessage.value = "删除任务失败: ${e.message}"
            }
        }
    }

    fun executeTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 更新任务状态为执行中
                val updatedTask = task.copy(
                    status = "RUNNING",
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateTask(updatedTask)
                
                // 获取服务器配置和token
                val config = dataStoreManager.serverConfig.first()
                val token = dataStoreManager.authToken.first()
                
                if (config.serverUrl.isNotBlank() && config.sendRoute.isNotBlank() && token.isNotBlank()) {
                    // 解析设备ID列表
                    val deviceIdList = try {
                        task.deviceIds.split(",").mapNotNull { it.toIntOrNull() }
                    } catch (e: Exception) {
                        emptyList()
                    }
                    
                    // 获取设备的实际deviceId（用户添加的设备ID）
                    val deviceDao = database.deviceDao()
                    val actualDeviceIds = mutableListOf<String>()
                    deviceIdList.forEach { deviceDbId ->
                        val device = deviceDao.getDeviceById(deviceDbId)
                        device?.deviceId?.let { actualDeviceIds.add(it) }
                    }
                    
                    // 获取指令信息
                    val command = database.commandDao().getCommandById(task.commandId)
                    val action = command?.jsFile ?: ""
                    
                    // 准备发送的数据
                    val taskData = mapOf(
                        "taskId" to task.id,
                        "taskName" to task.name,
                        "commandId" to task.commandId,
                        "action" to action,
                        "deviceIds" to actualDeviceIds,
                        "durationHours" to task.durationHours,
                        "durationMinutes" to task.durationMinutes,
                        "remark" to task.remark
                    )
                    
                    // 发送任务到服务器
                    val response = apiService.sendTaskToServer(
                        config.serverUrl,
                        config.sendRoute,
                        token,
                        taskData
                    )
                    
                    if (response.code == 0) {
                        // 发送成功，更新任务状态
                        val completedTask = updatedTask.copy(
                            status = "COMPLETED",
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.updateTask(completedTask)
                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                    } else {
                        // 发送失败，更新任务状态
                        val failedTask = updatedTask.copy(
                            status = "FAILED",
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.updateTask(failedTask)
                        _errorMessage.value = "任务执行失败: ${response.message}"
                    }
                } else {
                    // 配置不完整，更新任务状态为失败
                    val failedTask = updatedTask.copy(
                        status = "FAILED",
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateTask(failedTask)
                    _errorMessage.value = "服务器配置不完整或未登录"
                }
            } catch (e: Exception) {
                _errorMessage.value = "执行任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
