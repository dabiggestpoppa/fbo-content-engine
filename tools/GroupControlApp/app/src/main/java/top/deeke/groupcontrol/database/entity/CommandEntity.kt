package top.deeke.groupcontrol.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commands")
data class CommandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val title: String = "", // 从deekeScript.json的methods中获取
    val jsFile: String = "", // 从deekeScript.json的methods中获取
    val description: String = "",
    val content: String = "", // JSON格式的指令内容
    val order: Int = 0, // 排序位置
    val isPinned: Boolean = false // 是否置顶
)
