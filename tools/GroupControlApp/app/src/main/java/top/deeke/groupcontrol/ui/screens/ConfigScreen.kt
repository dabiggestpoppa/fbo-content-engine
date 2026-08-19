package top.deeke.groupcontrol.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import top.deeke.groupcontrol.data.DataStoreManager
import top.deeke.groupcontrol.model.ServerConfig
import top.deeke.groupcontrol.ui.theme.*

@Composable
fun ConfigScreen(
    onBack: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    val borderColor = if (isDarkTheme) BorderColor else BorderColorLight
    val primaryColor = if (isDarkTheme) NeonBlue else NeonCyan
    
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var serverUrl by remember { mutableStateOf("") }
    var requestFrequency by remember { mutableStateOf("5000") }
    var sendRoute by remember { mutableStateOf("/api/send") }
    var loginRoute by remember { mutableStateOf("/api/login") }
    
    // 加载配置
    LaunchedEffect(Unit) {
        dataStoreManager.serverConfig.collect { config ->
            serverUrl = config.serverUrl
            requestFrequency = config.requestFrequency.toString()
            sendRoute = config.sendRoute
            loginRoute = config.loginRoute
        }
    }
    
    // 自动保存函数
    fun saveConfig() {
        coroutineScope.launch {
            try {
                val config = ServerConfig(
                    serverUrl = serverUrl,
                    requestFrequency = requestFrequency.toIntOrNull() ?: 5000,
                    sendRoute = sendRoute,
                    loginRoute = loginRoute
                )
                dataStoreManager.saveServerConfig(config)
            } catch (e: Exception) {
                // 处理保存错误，可以添加错误提示
                println("保存配置失败: ${e.message}")
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部导航栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "服务器配置",
                color = textPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp)) // 占位，保持标题居中
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 服务器配置卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceVariantColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { 
                        serverUrl = it
                        saveConfig()
                    },
                    label = { Text("服务器地址", color = textSecondaryColor) },
                    placeholder = { Text("https://example.com", color = textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = requestFrequency,
                    onValueChange = { 
                        requestFrequency = it
                        saveConfig()
                    },
                    label = { Text("请求频率(毫秒)", color = textSecondaryColor) },
                    placeholder = { Text("5000", color = textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = sendRoute,
                    onValueChange = { 
                        sendRoute = it
                        saveConfig()
                    },
                    label = { Text("指令路由", color = textSecondaryColor) },
                    placeholder = { Text("/api/send", color = textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = loginRoute,
                    onValueChange = { 
                        loginRoute = it
                        saveConfig()
                    },
                    label = { Text("登录路由", color = textSecondaryColor) },
                    placeholder = { Text("/api/login", color = textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    )
                )


            }
        }

    }
}