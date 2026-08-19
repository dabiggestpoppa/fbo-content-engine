package top.deeke.groupcontrol.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import top.deeke.groupcontrol.database.entity.CommandEntity
import top.deeke.groupcontrol.database.entity.DeviceEntity
import top.deeke.groupcontrol.database.entity.TaskEntity
import top.deeke.groupcontrol.ui.theme.*
import top.deeke.groupcontrol.viewmodel.CommandViewModel
import top.deeke.groupcontrol.viewmodel.CommandViewModelFactory
import top.deeke.groupcontrol.viewmodel.DeviceViewModel
import top.deeke.groupcontrol.viewmodel.DeviceViewModelFactory
import top.deeke.groupcontrol.viewmodel.TaskViewModel
import top.deeke.groupcontrol.viewmodel.TaskViewModelFactory

@Composable
fun TaskScreen() {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    
    val context = LocalContext.current
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(context))
    val commandViewModel: CommandViewModel = viewModel(factory = CommandViewModelFactory(context))
    val deviceViewModel: DeviceViewModel = viewModel(factory = DeviceViewModelFactory(context))
    
    val tasks by taskViewModel.tasks.collectAsState()
    val commands by commandViewModel.commands.collectAsState()
    val devices by deviceViewModel.devices.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()
    val errorMessage by taskViewModel.errorMessage.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showExecuteConfirmDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }
    
    // 清除错误信息
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            taskViewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部操作栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "任务管理",
                color = textPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonBlue
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加任务",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "添加任务",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 错误信息显示
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f)
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
                        text = message,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // 任务列表或空状态
        if (tasks.isEmpty()) {
            // 空状态提示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "无任务",
                        modifier = Modifier.size(80.dp),
                        tint = textSecondaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "暂无任务",
                        color = textPrimaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "点击上方添加任务按钮\n创建您的第一个任务",
                        color = textSecondaryColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            // 任务列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        commands = commands,
                        devices = devices,
                        onExecute = { 
                            selectedTask = task
                            showExecuteConfirmDialog = true 
                        },
                        onDelete = { 
                            taskViewModel.deleteTask(task)
                        }
                    )
                }
            }
        }
    }
    
    // 添加任务对话框
    if (showAddDialog) {
        AddTaskDialog(
            commands = commands,
            devices = devices,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, remark, commandId, deviceIds, durationHours, durationMinutes ->
                taskViewModel.addTask(name, remark, commandId, deviceIds, durationHours, durationMinutes)
                showAddDialog = false
            }
        )
    }
    
    // 执行确认对话框
    if (showExecuteConfirmDialog && selectedTask != null) {
        AlertDialog(
            onDismissRequest = { showExecuteConfirmDialog = false },
            title = {
                Text(
                    text = "确认执行",
                    color = textPrimaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "确定要执行任务 \"${selectedTask!!.name}\" 吗？\n\n如果当前设备正在运行任务，将终止正在执行的任务。",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedTask?.let { task ->
                            taskViewModel.executeTask(task)
                        }
                        showExecuteConfirmDialog = false
                        selectedTask = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("确认执行", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showExecuteConfirmDialog = false
                    selectedTask = null
                }) {
                    Text("取消", color = textSecondaryColor)
                }
            }
        )
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    commands: List<CommandEntity>,
    devices: List<DeviceEntity>,
    onExecute: () -> Unit,
    onDelete: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val surfaceVariantColor = if (isDarkTheme) TechDarkSurfaceVariant else TechLightSurfaceVariant
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    // 获取关联的指令
    val command = commands.find { it.id == task.commandId }
    
    // 解析设备ID列表
    val deviceIdList = try {
        task.deviceIds.split(",").mapNotNull { it.toIntOrNull() }
    } catch (e: Exception) {
        emptyList()
    }
    
    // 获取关联的设备
    val selectedDevices = devices.filter { it.id in deviceIdList }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceVariantColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.name,
                    color = textPrimaryColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // 操作按钮
                Row {
                    if (task.status == "PENDING") {
                        IconButton(onClick = onExecute) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "执行",
                                tint = SuccessGreen
                            )
                        }
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = ErrorRed
                        )
                    }
                }
            }
            
            // 任务状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = when (task.status) {
                                "PENDING" -> WarningOrange
                                "RUNNING" -> SuccessGreen
                                "COMPLETED" -> NeonBlue
                                "FAILED" -> ErrorRed
                                else -> textSecondaryColor
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (task.status) {
                        "PENDING" -> "待执行"
                        "RUNNING" -> "执行中"
                        "COMPLETED" -> "已完成"
                        "FAILED" -> "执行失败"
                        else -> "未知状态"
                    },
                    color = when (task.status) {
                        "PENDING" -> WarningOrange
                        "RUNNING" -> SuccessGreen
                        "COMPLETED" -> NeonBlue
                        "FAILED" -> ErrorRed
                        else -> textSecondaryColor
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // 指令信息
            if (command != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "指令: ${command.title}${if (command.jsFile.isNotEmpty()) " (${command.jsFile})" else ""}",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
            
            // 设备信息
            if (selectedDevices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                var showDeviceDetails by remember { mutableStateOf(false) }
                
                TextButton(
                    onClick = { showDeviceDetails = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "设备: ${selectedDevices.size}台",
                        color = textSecondaryColor,
                        fontSize = 14.sp
                    )
                }
                
                // 设备详情对话框
                if (showDeviceDetails) {
                    AlertDialog(
                        onDismissRequest = { showDeviceDetails = false },
                        title = {
                            Text(
                                text = "设备详情",
                                color = textPrimaryColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            val clipboardManager = LocalClipboardManager.current
                            Column {
                                selectedDevices.forEach { device ->
                                    // 格式化设备ID，只显示后两段
                                    val formattedDeviceId = remember(device.deviceId) {
                                        val parts = device.deviceId.split("-")
                                        if (parts.size >= 2) {
                                            "${parts[parts.size - 2]}-${parts[parts.size - 1]}"
                                        } else {
                                            device.deviceId
                                        }
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = if (device.status == "ONLINE") SuccessGreen else ErrorRed,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = device.name,
                                            color = textPrimaryColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = formattedDeviceId,
                                            color = textSecondaryColor,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(device.deviceId))
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "复制设备ID",
                                                tint = textSecondaryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDeviceDetails = false }) {
                                Text("关闭", color = textSecondaryColor)
                            }
                        }
                    )
                }
            }
            
            // 执行时长信息
            if (task.durationHours > 0 || task.durationMinutes > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "执行时长: ${task.durationHours}小时${task.durationMinutes}分钟",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            }
            
            // 备注信息
            if (task.remark.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.remark,
                    color = textSecondaryColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            
            // 创建时间
            Spacer(modifier = Modifier.height(8.dp))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val createTime = dateFormat.format(Date(task.createdAt))
            Text(
                text = "创建时间: $createTime",
                color = textSecondaryColor,
                fontSize = 12.sp
            )
        }
    }
    
    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "确认删除",
                    color = textPrimaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "确定要删除任务 \"${task.name}\" 吗？",
                    color = textSecondaryColor,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("确认删除", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = textSecondaryColor)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    commands: List<CommandEntity>,
    devices: List<DeviceEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, remark: String, commandId: Int, deviceIds: List<Int>, durationHours: Int, durationMinutes: Int) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val borderColor = if (isDarkTheme) BorderColor else BorderColorLight
    val primaryColor = if (isDarkTheme) NeonBlue else NeonCyan
    
    var name by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var selectedCommandId by remember { mutableStateOf(0) }
    var selectedDeviceIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var durationHours by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("") }
    var showDeviceSelector by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加任务",
                color = textPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("任务名称", color = textSecondaryColor) },
                    placeholder = { Text("请输入任务名称", color = textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 指令选择
                if (commands.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = commands.find { it.id == selectedCommandId }?.let { cmd ->
                                "${cmd.title}${if (cmd.jsFile.isNotEmpty()) " (${cmd.jsFile})" else ""}"
                            } ?: "选择指令",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("指令", color = textSecondaryColor) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = borderColor,
                                focusedTextColor = textPrimaryColor,
                                unfocusedTextColor = textPrimaryColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            commands.forEach { command ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            "${command.title}${if (command.jsFile.isNotEmpty()) " (${command.jsFile})" else ""}", 
                                            color = textPrimaryColor
                                        ) 
                                    },
                                    onClick = {
                                        selectedCommandId = command.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "暂无可用指令，请先导入指令",
                        color = textSecondaryColor,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 设备选择按钮
                Button(
                    onClick = { showDeviceSelector = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = if (selectedDeviceIds.isEmpty()) "选择设备" else "已选择 ${selectedDeviceIds.size} 个设备",
                        color = textPrimaryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 执行时长设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = durationHours,
                        onValueChange = { 
                            // 只允许输入数字
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                durationHours = it
                            }
                        },
                        label = { Text("小时", color = textSecondaryColor) },
                        placeholder = { Text("0", color = textSecondaryColor) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        )
                    )
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { 
                            // 只允许输入数字，且限制在0-59范围内
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                val value = it.toIntOrNull() ?: 0
                                if (value <= 59) {
                                    durationMinutes = it
                                }
                            }
                        },
                        label = { Text("分钟", color = textSecondaryColor) },
                        placeholder = { Text("0", color = textSecondaryColor) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textPrimaryColor,
                            unfocusedTextColor = textPrimaryColor
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注", color = textSecondaryColor) },
                    placeholder = { Text("请输入备注 (可选)", color = textSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimaryColor,
                        unfocusedTextColor = textPrimaryColor
                    )
                )
                
                // 验证提示信息
                if (validationMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationMessage,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // 验证必填项
                    validationMessage = ""
                    when {
                        name.isBlank() -> {
                            validationMessage = "请输入任务名称"
                        }
                        selectedCommandId == 0 -> {
                            validationMessage = "请选择指令"
                        }
                        selectedDeviceIds.isEmpty() -> {
                            validationMessage = "请选择至少一个设备"
                        }
                        else -> {
                            // 验证通过，提交表单
                            val hours = durationHours.toIntOrNull() ?: 0
                            val minutes = durationMinutes.toIntOrNull() ?: 0
                            onConfirm(name, remark, selectedCommandId, selectedDeviceIds, hours, minutes)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("确定", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = textSecondaryColor)
            }
        }
    )
    
    // 设备选择对话框
    if (showDeviceSelector) {
        DeviceSelectorDialog(
            devices = devices,
            selectedDeviceIds = selectedDeviceIds,
            onDismiss = { showDeviceSelector = false },
            onConfirm = { deviceIds ->
                selectedDeviceIds = deviceIds
                showDeviceSelector = false
            }
        )
    }
}

@Composable
fun DeviceSelectorDialog(
    devices: List<DeviceEntity>,
    selectedDeviceIds: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    
    var tempSelectedIds by remember { mutableStateOf(selectedDeviceIds) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择设备",
                color = textPrimaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // 全选在线设备按钮
                val onlineDevices = devices.filter { it.status == "ONLINE" }
                if (onlineDevices.isNotEmpty()) {
                    Button(
                        onClick = {
                            tempSelectedIds = onlineDevices.map { it.id }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "全选在线设备 (${onlineDevices.size}个)",
                            color = textPrimaryColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 设备列表
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(devices) { device ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = device.id in tempSelectedIds,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        tempSelectedIds = tempSelectedIds + device.id
                                    } else {
                                        tempSelectedIds = tempSelectedIds - device.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = device.name,
                                color = textPrimaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (device.status == "ONLINE") SuccessGreen else ErrorRed,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSelectedIds) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("确定", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = textSecondaryColor)
            }
        }
    )
}
