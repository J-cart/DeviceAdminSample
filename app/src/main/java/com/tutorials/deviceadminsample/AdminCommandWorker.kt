package com.tutorials.deviceadminsample

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AdminCommandWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val data = inputData.getString(COMMAND_TYPE)
        val alarmTime = inputData.getLong(ALARM_TIME,0L)
        return try {
            when(data){
                LOCK->{
                    lockDevice(applicationContext)
                    Result.success()
                }
                ALARM->{
                    Log.d("CLOUD_MSG", "new time --->$alarmTime")
                    doAlarm(alarmTime)
                    Result.success()
                }
                else->Result.success()
            }

        }catch (e:Exception){
            Toast.makeText(applicationContext,"Work failed, check logcat",Toast.LENGTH_SHORT).show()
            Result.failure()
        }
    }

    private fun lockDevice(context: Context) {

        CoroutineScope(Dispatchers.Main).launch {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm.isScreenOn) {
                val policy =
                    context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                try {
                    policy.lockNow()
                } catch (ex: SecurityException) {
                    Toast.makeText(
                        context,
                        "You need to enable device administrator",
                        Toast.LENGTH_LONG
                    ).show()
                    val admin = ComponentName(context, SampleAdminReceiver::class.java)
                    val intent: Intent = Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                    ).putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin
                    )
                    context.startActivity(intent)
                }
            }
        }


    }

    private fun doAlarm(time:Long){
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)

    }
}