package top.deeke.groupcontrol.repository

import kotlinx.coroutines.flow.Flow
import top.deeke.groupcontrol.database.dao.DeviceDao
import top.deeke.groupcontrol.database.entity.DeviceEntity

class DeviceRepository(private val deviceDao: DeviceDao) {

    fun getAllDevices(): Flow<List<DeviceEntity>> = deviceDao.getAllDevices()

    suspend fun getDeviceById(id: Int): DeviceEntity? = deviceDao.getDeviceById(id)

    suspend fun insertDevice(device: DeviceEntity): Long = deviceDao.insertDevice(device)

    suspend fun updateDevice(device: DeviceEntity) = deviceDao.updateDevice(device)

    suspend fun deleteDevice(device: DeviceEntity) = deviceDao.deleteDevice(device)

    suspend fun deleteAllDevices() = deviceDao.deleteAllDevices()

    fun searchDevices(query: String): Flow<List<DeviceEntity>> = deviceDao.searchDevices(query)
}
