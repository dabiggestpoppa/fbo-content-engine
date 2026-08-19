package top.deeke.groupcontrol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import top.deeke.groupcontrol.MainActivity
import top.deeke.groupcontrol.R
import top.deeke.groupcontrol.data.DataStoreManager
import top.deeke.groupcontrol.model.ServerConfig
import top.deeke.groupcontrol.network.ApiService
import top.deeke.groupcontrol.utils.DeviceStatusUpdater

class CommandService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var dataStoreManager: DataStoreManager
    private val apiService = ApiService()
    private var currentJob: Job? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "command_service_channel"
        const val ACTION_START_SERVICE = "start_service"
        const val ACTION_STOP_SERVICE = "stop_service"
    }

    override fun onCreate() {
        super.onCreate()
        dataStoreManager = DataStoreManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("CommandService", "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                android.util.Log.d("CommandService", "Starting service")
                startService()
            }

            ACTION_STOP_SERVICE -> {
                android.util.Log.d("CommandService", "Stopping service")
                stopService()
            }
        }
        return START_STICKY
    }

    private fun startService() {
        android.util.Log.d("CommandService", "startService() called")
        try {
            startForeground(NOTIFICATION_ID, createNotification())
            android.util.Log.d("CommandService", "Foreground service started")

            currentJob?.cancel()
            currentJob = serviceScope.launch {
                android.util.Log.d("CommandService", "Starting to collect server config")
                dataStoreManager.serverConfig.collect { config ->
                    android.util.Log.d(
                        "CommandService",
                        "Server config collected: ${config.serverUrl}"
                    )
                    if (config.serverUrl.isNotBlank() && config.sendRoute.isNotBlank()) {
                        android.util.Log.d(
                            "CommandService",
                            "Starting polling with config: ${config.serverUrl}${config.sendRoute}"
                        )
                        startPolling(config)
                    } else {
                        android.util.Log.w(
                            "CommandService",
                            "Server config is incomplete, skipping polling"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CommandService", "Error starting service: ${e.message}")
        }
    }

    private fun stopService() {
        currentJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun startPolling(config: ServerConfig) {
        android.util.Log.d(
            "CommandService",
            "startPolling() called with frequency: ${config.requestFrequency}ms"
        )
        val deviceStatusUpdater = DeviceStatusUpdater(this)

        while (serviceScope.isActive) {
            try {
                // 获取当前token
                val currentToken = dataStoreManager.authToken.first()

                // 获取所有设备的deviceId列表
                val allDevices = dataStoreManager.getAllDevices().first()
                val deviceIds: List<String> =
                    allDevices.map { it.deviceId }.filter { it.isNotBlank() }

                android.util.Log.d(
                    "CommandService",
                    "Making API call with token: ${if (currentToken.isNotBlank()) "present" else "empty"}, devices: $deviceIds"
                )
                val response = apiService.checkServerConfig(
                    config.serverUrl,
                    config.sendRoute,
                    currentToken,
                    deviceIds
                )
                android.util.Log.d(
                    "CommandService",
                    "API response: code=${response.code}, message=${response.message}"
                )

                // 更新设备状态
                if (response.code == 0 && response.data != null) {
                    Log.d("debug", "设备信息：${response.data}");
                    deviceStatusUpdater.updateDeviceStatuses(response.data)
                }

                // 更新通知显示最新状态
                val notification = createNotification("远程服务运行中...")
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)

                // 如果返回401或402，停止服务（需要重新登录）
                if (response.code == 401 || response.code == 402) {
                    android.util.Log.w(
                        "CommandService",
                        "Authentication failed (${response.code}), stopping service"
                    )
                    // 清除无效token
                    dataStoreManager.clearAuthToken()
                    stopService()
                    break
                }

            } catch (e: Exception) {
                android.util.Log.e("CommandService", "Error during polling: ${e.message}")
                // 网络错误时也更新通知
                val notification = createNotification("网络错误: ${e.message}")
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }

            // 等待配置的间隔时间
            delay(config.requestFrequency.toLong())
        }
        android.util.Log.d("CommandService", "Polling loop ended")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "远程服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "定时请求服务器配置"
            setShowBadge(false)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(status: String = "服务运行中"): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("远程服务")
            .setContentText(status)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
