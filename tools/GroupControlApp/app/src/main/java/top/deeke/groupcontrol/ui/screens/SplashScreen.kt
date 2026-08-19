package top.deeke.groupcontrol.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import top.deeke.groupcontrol.R
import top.deeke.groupcontrol.data.DataStoreManager
import top.deeke.groupcontrol.network.ApiService
import top.deeke.groupcontrol.service.CommandService
import top.deeke.groupcontrol.ui.theme.*

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val primaryColor = if (isDarkTheme) NeonBlue else NeonCyan
    val surfaceColor = if (isDarkTheme) TechDarkSurface else TechLightSurface

    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val apiService = remember { ApiService() }

    var isLoading by remember { mutableStateOf(true) }
    var statusText by remember { mutableStateOf("正在启动...") }

    // 启动页接口检查
    LaunchedEffect(Unit) {
        delay(1500) // 显示启动页1.5秒

        try {
            dataStoreManager.serverConfig.collect { config ->
                if (config.serverUrl.isNotBlank() && config.sendRoute.isNotBlank()) {
                    // 有配置信息，检查服务器状态
                    statusText = "正在检查服务器状态..."
                    val response = apiService.checkServerConfig(config.serverUrl, config.sendRoute, dataStoreManager.authToken.first())
                    Log.d("debug", "登录验证返回：$response");
                    when (response.code) {
                        401, 402 -> {
                            statusText = "需要登录验证"
                            delay(500)
                            onNavigateToLogin()
                        }

                        0 -> {
                            statusText = "启动完成"
                            // 启动CommandService服务
                            try {
                                val serviceIntent = Intent(context, CommandService::class.java).apply {
                                    action = CommandService.ACTION_START_SERVICE
                                }
                                context.startForegroundService(serviceIntent)
                                Log.d("debug", "SplashScreen: CommandService启动成功")
                            } catch (e: Exception) {
                                Log.e("debug", "SplashScreen: CommandService启动失败: ${e.message}")
                            }
                            delay(500)
                            onNavigateToMain()
                        }

                        else -> {
                            statusText = "指令路由请求失败"
                            delay(500)
                            onNavigateToLogin()
                        }
                    }
                } else {
                    // 首次安装，没有配置信息，直接进入登录页面
                    statusText = "首次使用，请先配置"
                    delay(500)
                    onNavigateToLogin()
                }
                isLoading = false
            }
        } catch (e: Exception) {
            statusText = "启动完成"
            delay(500)
            onNavigateToLogin() // 出错时也进入登录页面
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // APP图标
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // APP名称
            Text(
                text = "远程控制端",
                color = if (isDarkTheme) TextPrimary else TextPrimaryLight,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "控制远程设备执行任务",
                color = if (isDarkTheme) TextSecondary else TextSecondaryLight,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 加载指示器
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = primaryColor,
                    strokeWidth = 3.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = statusText,
                    color = if (isDarkTheme) TextSecondary else TextSecondaryLight,
                    fontSize = 14.sp
                )
            }
        }
    }
}
