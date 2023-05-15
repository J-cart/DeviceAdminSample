package com.tutorials.deviceadminsample.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.map


const val VERBOSE_NOTIFICATION_CHANNEL_NAME = "Verbose FCM Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications whenever FCM sends messages"
const val NOTIFICATION_TITLE = "FCM TESTING"
const val CHANNEL_ID = "DEVICE_ADMIN_SAMPLE_NOTIFICATION"
const val WEB_KEY = "key =AAAAU4NEKao:APA91bHMiLRgIq8fQdoePbdWK9HuO7TwdheEVEshLC7GxT7VHy_4j1nMazZ1h0Y9_irzbU_rk5hnrPWbSk47APU0gYFVtgzA4SKZgyiMAUCfHq0xvm8ALvlfv0ZdzEgLOVq7GMvL_cig"
const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
const val USERS ="ADMIN-LOCK"
const val ADMIN_ACCESS ="ADMIN-ACCESS"
const val ACTION_GIF_URL ="https://s3-alpha-sig.figma.com/img/b9ed/7dd2/8b8c341a410790f8d4f8653e1e56497a?Expires=1684713600&Signature=QIHDeJ5t6tCzdYQR1KqjeIM~7YmAdEPVedw4kVPI-ehMK45yDHLxfy3w4fxjcJF2uD7p~m-ORo6nXllTxj9peP8Ic9S4Fv5Q1mYj19~5iDpr3EPX-oSeCmln-PYzGoULO-728neE0rNJkXSLhfLlCjiMoZMUs9kBdv5LvfV6ij4w3McaLSze8pQ8HDllru1OaLfOZkrnHIhbpmOLdHwwF4OfrFm9taj8weTaVvbUxs3oNXsvi6hUe4I0vgdxaJtrGaK0H8y3Rx9AEmRbH~ifSAXc~7gmevPH2qg1XtDbSIWLDYjQ86bQwr0mRbruCy80b0ZW~eGUtlmPMHVglAylmQ__&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4"
const val AUTH_GIF_URL ="https://s3-alpha-sig.figma.com/img/c9be/b1cd/3b949ee607f4b0a6e7c9692fb987aa25?Expires=1684713600&Signature=giLO0dVLpnZ3IUc4XjONrZ~FeGfoJv5sbpePyUv7-dNMiCb40FMDVqaNH8iG190d-L8~2z~kHuuaL-SgRYURAySp6~hDBQIxazMQmz56qbWbpwNIw~rc8lYtSTneb3eExKXbagWqiShHZiSDoRKAuk7UsHUKT-HV2m9GwlcdHSTwtcxT7Ao7EG09~qx8JRqmkQ-zhc9OwwwwHbd~w3~L0QpXWsKT9O275lkLEG7pWmJ2n50AcevtHhNQIfKMbCSuVYBEZ1esFKfgAkS1kmf8nq5H-9WF1uL8~dD2RzHJL6PzficNd0T2LLCTIzkeZvE0cXs4XIlWsa5xL8dsgtW4PQ__&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4"
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
        fun getFirstLaunch(key: String?, defValue: Boolean): Boolean {
            return mSharedPref!!.getBoolean(key, defValue)
        }
        fun putFirstLaunch(key: String?, value: Boolean) {
            val prefsEditor = mSharedPref!!.edit()
            prefsEditor.putBoolean(key, value)
            prefsEditor.apply()
        }
    }


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
