package com.tutorials.deviceadminsample

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SampleAdminReceiver:DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context,"Admin Enabled",Toast.LENGTH_SHORT).show()
//        LocalBroadcastManager.getInstance(context).sendBroadcast(
//             Intent(ACTION_DEVICE_ADMIN_ENABLED))
        SharedPreference.init(context)
        SharedPreference.putBoolean(ADMIN_ACCESS,true)

    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context,"Admin Disabled",Toast.LENGTH_SHORT).show()
//        LocalBroadcastManager.getInstance(context).sendBroadcast(
//            Intent(ACTION_DEVICE_ADMIN_DISABLED))
        SharedPreference.init(context)
        SharedPreference.putBoolean(ADMIN_ACCESS,false)
    }
}