package com.tutorials.deviceadminsample.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tutorials.deviceadminsample.*
import com.tutorials.deviceadminsample.model.DeviceInfo
import com.tutorials.deviceadminsample.model.User
import com.tutorials.deviceadminsample.util.*
import com.tutorials.deviceadminsample.worker.AdminCommandWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private val fStoreUsers = Firebase.firestore.collection(USERS).document(USER).collection("ALL")

class FirebaseMessagingReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        makeStatusNotification(message.data.toString(), this)
        Log.d("CLOUD_MSG", "${message.data}")
        Log.d("CLOUD_MSG", "messageType--> ${message.data["messageType"]}")


        when (message.data["commandType"]) {
            ALARM -> {
                upcomingAlarmTime.value = message.data["alarmTime"]!!.toLong()
                val alarmData =
                    Data.Builder().putString(COMMAND_TYPE, ALARM)
                        .putLong(ALARM_TIME, message.data["alarmTime"]!!.toLong()).build()
                val workOne =
                    OneTimeWorkRequestBuilder<AdminCommandWorker>().setInputData(alarmData)
                        .build()
                WorkManager.getInstance(this)
                    .beginUniqueWork("TestWork", ExistingWorkPolicy.KEEP, workOne)
                    .enqueue()

            }
            LOCK -> {
                val lockData =
                    Data.Builder().putString(COMMAND_TYPE, LOCK).build()
                val workOne = OneTimeWorkRequestBuilder<AdminCommandWorker>().setInputData(lockData)
                    .build()
                WorkManager.getInstance(this)
                    .beginUniqueWork("TestWork", ExistingWorkPolicy.KEEP, workOne)
                    .enqueue()

            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("CLOUD_MSG", "new token --->$token")
        updateDeviceToken(this,token)
    }


    private fun makeStatusNotification(message: String, context: Context) {

        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
            val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            // Add the channel
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        // Show the notification
        NotificationManagerCompat.from(context).notify(300, builder.build())
    }

    companion object {
        var upcomingAlarmTime = MutableStateFlow(0L)

        fun updateDeviceToken(context: Context, token: String,deviceInfo: DeviceInfo= DeviceInfo()) {
            Firebase.auth.currentUser?.let { currentUser ->
                currentUser.email?.let { email ->
                    CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
                        try {

                            val getUser =
                                fStoreUsers.document(email).collection(DEVICES).document(deviceInfo.deviceId)
                                    .get()
                                    .await()
                            if (getUser.exists()) {
                                getUser.toObject<DeviceInfo>()?.let { deviceInfo ->
                                    val tokenList = mutableListOf<String>()
                                    tokenList.addAll(deviceInfo.deviceToken)
                                    tokenList.add(token)
                                    fStoreUsers.document(email).collection(DEVICES)
                                        .document(deviceInfo.deviceId)
                                        .update("deviceToken", tokenList)
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                context.showToast("Token Updated Successfully")
                                                Log.d("me_updateToken", " ${it.result}")
                                            } else {
                                                context.showToast("Error:Token Not Updated:->${it.exception}")
                                                Log.d("me_updateToken", " ${it.exception}")
                                            }

                                        }
                                }
                                return@launch
                            }
                           fStoreUsers.document(email).collection(DEVICES).document(deviceInfo.deviceId).set(deviceInfo).await()
                            Log.d("me_updateToken", " Document Doesn't exist but updating new info...")
                            context.showToast("Oops...Document Doesn't exist but updating new info...")
                        }catch (e:Exception){
                            Log.d("me_updateToken", "Exception:-> $e")
                            context.showToast("Ooops...Token Not Updated:->$e")

                        }
                    }
                }
            }
        }

    }

}


