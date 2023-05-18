package com.tutorials.deviceadminsample.model

data class DeviceInfo(
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceToken: List<String> = emptyList(),
    val location: String = "",
)