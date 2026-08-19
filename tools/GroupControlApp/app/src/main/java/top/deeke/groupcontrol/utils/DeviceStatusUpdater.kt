package top.deeke.groupcontrol.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject
import top.deeke.groupcontrol.database.AppDatabase

class DeviceStatusUpdater(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val deviceDao = database.deviceDao()

    suspend fun updateDeviceStatuses(statusData: Any?) {
        try {
            if (statusData is JSONObject) {
                val statusMap = mutableMapOf<String, String>()
                
                // 解析JSON对象，获取每个设备的状态
                statusData.keys().forEach { deviceId ->
                    val status = statusData.optString(deviceId, "OFFLINE")
                    statusMap[deviceId] = status
                }
                
                Log.d("debug", "解析到设备状态: $statusMap")
                
                // 更新本地设备状态
                statusMap.forEach { (deviceId, status) ->
                    val device = deviceDao.getDeviceByDeviceId(deviceId)
                    if (device != null) {
                        val newStatus = when (status.lowercase()) {
                            "online" -> "ONLINE"
                            "offline" -> "OFFLINE"
                            "idle" -> "IDLE"
                            else -> "UNKNOWN"
                        }
                        
                        if (device.status != newStatus) {
                            val updatedDevice = device.copy(
                                status = newStatus,
                                updatedAt = System.currentTimeMillis()
                            )
                            deviceDao.updateDevice(updatedDevice)
                            Log.d("debug", "更新设备状态: $deviceId -> $newStatus")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("debug", "更新设备状态失败: ${e.message}")
        }
    }
}
