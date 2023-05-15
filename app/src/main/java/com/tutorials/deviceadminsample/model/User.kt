package com.tutorials.deviceadminsample.model

data class User(
    val email: String = "",
    val uid: String = "",
    val password: String = "",
)

data class DeviceInfo(
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceToken: List<String> = emptyList(),
    val location: String = "",
)


data class RemoteCommand(val type: String = "", val data: String = "")