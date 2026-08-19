package top.deeke.groupcontrol.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import top.deeke.groupcontrol.database.entity.DeviceEntity

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY createdAt DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE deviceId = :deviceId")
    suspend fun getDeviceByDeviceId(deviceId: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getDeviceById(id: Int): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity): Long

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Delete
    suspend fun deleteDevice(device: DeviceEntity)

    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()

    @Query("SELECT * FROM devices WHERE name LIKE :query OR remark LIKE :query OR deviceId LIKE :query")
    fun searchDevices(query: String): Flow<List<DeviceEntity>>
}
