package com.tutorials.deviceadminsample.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder


const val VERBOSE_NOTIFICATION_CHANNEL_NAME = "Verbose FCM Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications whenever FCM sends messages"
const val NOTIFICATION_TITLE = "Remote Locker"
const val CHANNEL_ID = "DEVICE_ADMIN_SAMPLE_NOTIFICATION"
const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
const val USERS ="ADMIN-LOCK"
const val ADMIN_ACCESS ="ADMIN-ACCESS"

fun Context.showToast(text:String){
    Toast.makeText(this,text, Toast.LENGTH_SHORT).show()
}

fun Context.showAlert(title:String,msg:String,action:()->Unit){
    MaterialAlertDialogBuilder(this).apply {
        setMessage(msg)
        setTitle(title)
        setPositiveButton("OK") { dialogInterface, int ->
            action()
            dialogInterface.dismiss()
        }
        create()
        show()
    }
}
fun Context.showStrictAlert(title:String,msg:String,action:()->Unit){
    MaterialAlertDialogBuilder(this).apply {
        setMessage(msg)
        setTitle(title)
        setCancelable(false)
        setPositiveButton("OK") { dialogInterface, int ->
            action()
            setCancelable(true)
            dialogInterface.dismiss()
        }
        create()
        show()
    }
}

fun Context.checkPermissions(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

}
fun requestPermissions(permissionsRequestLauncher: ActivityResultLauncher<Array<String>>) {
    permissionsRequestLauncher.launch(
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )
}

const val FIRST_LAUNCH = "FIRST_LAUNCH"

const val DATE_FORMAT_ONE="dd-MM-yyyy"
const val TIME_FORMAT_ONE="hh:mm a"

const val USER ="Regular User"
const val ADMIN = "Administrator"
const val LOCK = "Lock Device"
const val ALARM = "Set Alarm"
const val COMMAND_TYPE = "Command Type"
const val ALARM_TIME = "Alarm Time"
const val ACTIVE = "Logged In"
const val INACTIVE = "Logged Out"
const val DEVICES = "Devices"
const val COMMANDS = "Device Commands"
const val WORK_NAME = "Remote Locker Worker"
