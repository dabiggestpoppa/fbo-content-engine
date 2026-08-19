package top.deeke.groupcontrol.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.deeke.groupcontrol.R
import top.deeke.groupcontrol.ui.theme.NeonBlue
import top.deeke.groupcontrol.ui.theme.NeonCyan
import top.deeke.groupcontrol.ui.theme.TechDark
import top.deeke.groupcontrol.ui.theme.TechDarkSurface
import top.deeke.groupcontrol.ui.theme.TechLight
import top.deeke.groupcontrol.ui.theme.TechLightSurface
import top.deeke.groupcontrol.ui.theme.TextPrimary
import top.deeke.groupcontrol.ui.theme.TextPrimaryLight
import top.deeke.groupcontrol.ui.theme.TextSecondary
import top.deeke.groupcontrol.ui.theme.TextSecondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    username: String = "",
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current

    // 从strings.xml中获取app_name
    val appName = context.getString(R.string.app_name)

    // 调整标签页顺序：设备、指令、任务、配置
    val tabs = listOf("设备", "指令", "任务", "配置")

    // 根据主题选择颜色
    val backgroundColor = if (isDarkTheme) TechDark else TechLight
    val surfaceColor = if (isDarkTheme) TechDarkSurface else TechLightSurface
    val textPrimaryColor = if (isDarkTheme) TextPrimary else TextPrimaryLight
    val textSecondaryColor = if (isDarkTheme) TextSecondary else TextSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = appName,
                    color = textPrimaryColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopEnd),
                    contentAlignment = Alignment.TopEnd
                ) {
                    var anchorWidthPx by remember { mutableStateOf(0) }
                    val density = LocalDensity.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expanded = true }
                            .onGloballyPositioned { layoutCoordinates ->
                                anchorWidthPx = layoutCoordinates.size.width
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "用户",
                            tint = if (isDarkTheme) NeonBlue else NeonCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // 安全的账号脱敏显示：中间4位用*，长度不足则原样
                        Text(
                            text = if (username.isNotBlank()) {
                                if (username.length > 7) {
                                    username.replaceRange(3, (username.length - 4).coerceAtLeast(3), "****")
                                } else username
                            } else "未登录",
                            color = textPrimaryColor,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "更多"
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(with(density) { anchorWidthPx.toDp() })
                    ) {
                        DropdownMenuItem(
                            text = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) { Text("退出") }
                            },
                            onClick = {
                                expanded = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = surfaceColor
            )
        )

        // 标签页
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = surfaceColor,
            contentColor = if (isDarkTheme) NeonBlue else NeonCyan
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) {
                                if (isDarkTheme) NeonBlue else NeonCyan
                            } else {
                                textSecondaryColor
                            }
                        )
                    }
                )
            }
        }

        // 内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(backgroundColor, surfaceColor)
                    )
                )
        ) {
            when (selectedTab) {
                0 -> DeviceScreen() // 设备管理
                1 -> CommandScreen() // 指令管理
                2 -> TaskScreen() // 任务管理
                3 -> ConfigScreen() // 服务器配置
            }
        }
    }
}