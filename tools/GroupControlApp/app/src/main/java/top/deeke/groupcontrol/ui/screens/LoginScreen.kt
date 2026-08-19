package top.deeke.groupcontrol.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.deeke.groupcontrol.data.DataStoreManager
import top.deeke.groupcontrol.model.ServerConfig
import top.deeke.groupcontrol.ui.theme.*

@Composable
fun LoginDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onLogin: (username: String, password: String, onComplete: (success: Boolean, message: String?) -> Unit) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val borderColor = if (isDarkTheme) BorderColor else BorderColorLight
    val primaryColor = if (isDarkTheme) NeonBlue else NeonCyan
    
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var hasConfig by remember { mutableStateOf(false) }
    var showConfigAlert by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 配置表单状态
    var serverUrl by remember { mutableStateOf("") }
    var requestFrequency by remember { mutableStateOf("5000") }
    var sendRoute by remember { mutableStateOf("/api/send") }
    var loginRoute by remember { mutableStateOf("/api/login") }
    var showConfigForm by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // 保存配置函数
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
                println("保存配置失败: ${e.message}")
            }
        }
    }
    
    // 检查是否有配置信息
    LaunchedEffect(isVisible) {
        if (isVisible) {
            dataStoreManager.serverConfig.collect { config ->
                hasConfig = config.serverUrl.isNotBlank() && config.sendRoute.isNotBlank()
                // 加载配置信息到表单
                serverUrl = config.serverUrl
                requestFrequency = config.requestFrequency.toString()
                sendRoute = config.sendRoute
                loginRoute = config.loginRoute
            }
        }
    }
    
    if (isVisible) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            title = {
                Text(
                    text = "登录",
                    color = textPrimaryColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 配置按钮
                    Button(
                        onClick = { showConfigForm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "配置服务器",
                            color = textPrimaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // 错误信息显示
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) ErrorRed.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "错误",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = ErrorRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("账号", color = textSecondaryColor) },
                        placeholder = { Text("请输入账号", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        )
                    )
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码", color = textSecondaryColor) },
                        placeholder = { Text("请输入密码", color = textSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!hasConfig) {
                            showConfigAlert = true
                        } else if (username.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            errorMessage = null // 清除之前的错误信息
                            onLogin(username, password) { success, message ->
                                isLoading = false
                                if (!success) {
                                    errorMessage = message
                                }
                            }
                        }
                    },
                    enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = TextPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isLoading) "登录中..." else "登录",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("关闭APP", color = textSecondaryColor)
                }
            }
        )
        
        // 配置弹窗
        if (showConfigForm) {
            AlertDialog(
                onDismissRequest = { showConfigForm = false },
                title = {
                    Text(
                        text = "服务器配置",
                        color = textPrimaryColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                },
                confirmButton = {
                    Button(
                        onClick = { showConfigForm = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "完成",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfigForm = false }) {
                        Text("取消", color = textSecondaryColor)
                    }
                }
            )
        }
        
        // 配置提示对话框
        if (showConfigAlert) {
            AlertDialog(
                onDismissRequest = { showConfigAlert = false },
                title = {
                    Text(
                        text = "提示",
                        color = textPrimaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "请先配置服务器信息后再进行登录",
                        color = textSecondaryColor,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfigAlert = false
                            showConfigForm = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("去配置", color = TextPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfigAlert = false }) {
                        Text("取消", color = textSecondaryColor)
                    }
                }
            )
        }
    }
}