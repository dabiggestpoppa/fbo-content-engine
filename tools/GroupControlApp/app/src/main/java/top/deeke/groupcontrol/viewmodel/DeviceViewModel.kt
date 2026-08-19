package top.deeke.groupcontrol.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.deeke.groupcontrol.database.AppDatabase
import top.deeke.groupcontrol.database.entity.DeviceEntity
import top.deeke.groupcontrol.repository.DeviceRepository

class DeviceViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val repository = DeviceRepository(database.deviceDao())

    private val _devices = MutableStateFlow<List<DeviceEntity>>(emptyList())
    val devices: StateFlow<List<DeviceEntity>> = _devices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadDevices()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            repository.getAllDevices().collect { deviceList ->
                _devices.value = deviceList
            }
        }
    }

    fun addDevice(name: String, remark: String, deviceId: String) {
        viewModelScope.launch {
            try {
                val device = DeviceEntity(
                    name = name,
                    remark = remark,
                    deviceId = deviceId,
                    status = "OFFLINE",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.insertDevice(device)
            } catch (e: Exception) {
                _errorMessage.value = "添加设备失败: ${e.message}"
            }
        }
    }

    fun updateDevice(device: DeviceEntity) {
        viewModelScope.launch {
            try {
                val updatedDevice = device.copy(updatedAt = System.currentTimeMillis())
                repository.updateDevice(updatedDevice)
            } catch (e: Exception) {
                _errorMessage.value = "更新设备失败: ${e.message}"
            }
        }
    }

    fun deleteDevice(device: DeviceEntity) {
        viewModelScope.launch {
            try {
                repository.deleteDevice(device)
            } catch (e: Exception) {
                _errorMessage.value = "删除设备失败: ${e.message}"
            }
        }
    }

    fun deleteAllDevices() {
        viewModelScope.launch {
            try {
                repository.deleteAllDevices()
            } catch (e: Exception) {
                _errorMessage.value = "清空设备失败: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
