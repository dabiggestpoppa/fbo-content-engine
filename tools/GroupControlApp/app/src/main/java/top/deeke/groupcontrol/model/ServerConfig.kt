package top.deeke.groupcontrol.model

import android.os.Parcelable

data class ServerConfig(
    val id: Int = 0,
    val serverUrl: String = "",
    val requestFrequency: Int = 5000, // 毫秒
    val sendRoute: String = "/api/send",
    val loginRoute: String = "/api/login"
) : Parcelable {
    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: android.os.Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(serverUrl)
        dest.writeInt(requestFrequency)
        dest.writeString(sendRoute)
        dest.writeString(loginRoute)
    }
    
    companion object {
        @JvmField
        val CREATOR = object : android.os.Parcelable.Creator<ServerConfig> {
            override fun createFromParcel(parcel: android.os.Parcel): ServerConfig {
                return ServerConfig(
                    id = parcel.readInt(),
                    serverUrl = parcel.readString() ?: "",
                    requestFrequency = parcel.readInt(),
                    sendRoute = parcel.readString() ?: "",
                    loginRoute = parcel.readString() ?: ""
                )
            }
            
            override fun newArray(size: Int): Array<ServerConfig?> = arrayOfNulls(size)
        }
    }
}