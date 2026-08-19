package top.deeke.groupcontrol.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.deeke.groupcontrol.database.AppDatabase
import top.deeke.groupcontrol.database.entity.DeviceEntity
import top.deeke.groupcontrol.model.ServerConfig

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    
    // 服务器配置相关
    private val serverUrlKey = stringPreferencesKey("server_url")
    private val requestFrequencyKey = intPreferencesKey("request_frequency")
    private val sendRouteKey = stringPreferencesKey("send_route")
    private val loginRouteKey = stringPreferencesKey("login_route")
    
    // 用户认证相关
    private val tokenKey = stringPreferencesKey("auth_token")
    private val usernameKey = stringPreferencesKey("username")
    
    // 获取服务器配置
    val serverConfig: Flow<ServerConfig> = context.dataStore.data.map { preferences ->
        ServerConfig(
            serverUrl = preferences[serverUrlKey] ?: "",
            requestFrequency = preferences[requestFrequencyKey] ?: 5000,
            sendRoute = preferences[sendRouteKey] ?: "/api/send",
            loginRoute = preferences[loginRouteKey] ?: "/api/login"
        )
    }
    
    // 保存服务器配置
    suspend fun saveServerConfig(config: ServerConfig) {
        context.dataStore.edit { preferences ->
            preferences[serverUrlKey] = config.serverUrl
            preferences[requestFrequencyKey] = config.requestFrequency
            preferences[sendRouteKey] = config.sendRoute
            preferences[loginRouteKey] = config.loginRoute
        }
    }
    
    // 获取认证token
    val authToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[tokenKey] ?: ""
    }

    // 获取用户名
    val username: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[usernameKey] ?: ""
    }
    
    // 保存认证token
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    // 保存用户名
    suspend fun saveUsername(name: String) {
        context.dataStore.edit { preferences ->
            preferences[usernameKey] = name
        }
    }
    
    // 清除认证token
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }

    // 清除用户名
    suspend fun clearUsername() {
        context.dataStore.edit { preferences ->
            preferences.remove(usernameKey)
        }
    }
    
    suspend fun getAllDevices(): Flow<List<DeviceEntity>> {
        val database = AppDatabase.getDatabase(context)
        return database.deviceDao().getAllDevices()
    }
}