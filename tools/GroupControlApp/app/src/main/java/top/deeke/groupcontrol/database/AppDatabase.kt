package top.deeke.groupcontrol.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import top.deeke.groupcontrol.database.dao.CommandDao
import top.deeke.groupcontrol.database.dao.DeviceDao
import top.deeke.groupcontrol.database.dao.TaskDao
import top.deeke.groupcontrol.database.entity.CommandEntity
import top.deeke.groupcontrol.database.entity.DeviceEntity
import top.deeke.groupcontrol.database.entity.TaskEntity

@Database(
    entities = [CommandEntity::class, DeviceEntity::class, TaskEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    abstract fun deviceDao(): DeviceDao
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // 数据库迁移策略
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建设备表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `devices` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL DEFAULT '',
                        `remark` TEXT NOT NULL DEFAULT '',
                        `deviceId` TEXT NOT NULL DEFAULT '',
                        `status` TEXT NOT NULL DEFAULT 'OFFLINE',
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建任务表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL DEFAULT '',
                        `remark` TEXT NOT NULL DEFAULT '',
                        `commandId` INTEGER NOT NULL DEFAULT 0,
                        `deviceIds` TEXT NOT NULL DEFAULT '',
                        `status` TEXT NOT NULL DEFAULT 'PENDING',
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加执行时长字段
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `durationHours` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `tasks` ADD COLUMN `durationMinutes` INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "group_control_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration() // 如果迁移失败，允许破坏性迁移
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
