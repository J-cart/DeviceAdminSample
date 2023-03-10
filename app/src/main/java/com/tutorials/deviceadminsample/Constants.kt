package com.tutorials.deviceadminsample

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder


const val VERBOSE_NOTIFICATION_CHANNEL_NAME = "Verbose FCM Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications whenever FCM sends messages"
const val NOTIFICATION_TITLE = "FCM TESTING"
const val CHANNEL_ID = "DEVICE_ADMIN_SAMPLE_NOTIFICATION"
const val WEB_KEY = "key =AAAAU4NEKao:APA91bHMiLRgIq8fQdoePbdWK9HuO7TwdheEVEshLC7GxT7VHy_4j1nMazZ1h0Y9_irzbU_rk5hnrPWbSk47APU0gYFVtgzA4SKZgyiMAUCfHq0xvm8ALvlfv0ZdzEgLOVq7GMvL_cig"
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

class SharedPreference {
    companion object{
        private var mSharedPref: SharedPreferences? = null

        fun init(context: Context) {
            if (mSharedPref == null) mSharedPref =
                context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
        }
        fun getBoolean(key: String?, defValue: Boolean): Boolean {
            return mSharedPref!!.getBoolean(key, defValue)
        }
        fun putBoolean(key: String?, value: Boolean) {
            val prefsEditor = mSharedPref!!.edit()
            prefsEditor.putBoolean(key, value)
            prefsEditor.apply()
        }
    }


}

const val DATE_FORMAT_ONE="dd-MM-yyyy"
const val TIME_FORMAT_ONE="hh:mm a"

const val USER ="Regular User"
const val ADMIN = "Administrator"
const val LOCK = "Lock Device"
const val ALARM = "Set Alarm"
const val COMMAND_TYPE = "Command Type"
const val ALARM_TIME = "Alarm Time"
