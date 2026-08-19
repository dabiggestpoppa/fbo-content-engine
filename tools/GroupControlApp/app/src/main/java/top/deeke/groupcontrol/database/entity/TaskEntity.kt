package top.deeke.groupcontrol.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val remark: String = "",
    val commandId: Int = 0, // 关联的指令ID
    val deviceIds: String = "", // JSON格式存储设备ID列表
    val durationHours: Int = 0, // 执行时长-小时
    val durationMinutes: Int = 0, // 执行时长-分钟
    val status: String = "PENDING", // PENDING, RUNNING, COMPLETED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
