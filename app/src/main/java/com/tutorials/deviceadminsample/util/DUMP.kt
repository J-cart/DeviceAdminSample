package com.tutorials.deviceadminsample.util

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.tutorials.deviceadminsample.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/*
 private val fStoreUsers1 = Firebase.firestore.collection(USERS).document(USER).collection("ALL")
        .document("email").collection("devices").document("--").collection("command").document("all--")*/
fun getData(): ArrayList<User> {
    val items = ArrayList<User>()
    return items.apply {
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
        add(
            User(
                email = "Some random device",
                password = "Not active"
            )
        )
    }
}


/*
fun new(){
    //to Unlock... I guess
    val km = getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
    val kl = km.newKeyguardLock("MyKeyguardLock")
    kl.disableKeyguard()

    val pm = getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
    val wakeLock = pm.newWakeLock(
        PowerManager.FULL_WAKE_LOCK
                or PowerManager.ACQUIRE_CAUSES_WAKEUP
                or PowerManager.ON_AFTER_RELEASE, ":MyWakeLock"
    )

    wakeLock.acquire(1*60*1000L */
/*10 minutes*//*
)
}

private fun doTokenOperation(){
    binding.statusTv.text = "getting token...."

    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask->
        binding.statusTv.text = "loading...."
        if (tokenTask.isSuccessful){
            binding.statusTv.text = "token gotten successfully"
            deviceToken = tokenTask.result
            val user = User("Device", tokenTask.result.toString())
            fireStoreRef.document(System.currentTimeMillis().toString()).set(user).addOnCompleteListener { setDocTask->
                binding.statusTv.text = "uploading token..."

                if (setDocTask.isSuccessful){
                    binding.statusTv.text = "uploading token successful"
                    Log.d("NEW_TASK", "success-- ${setDocTask.result}")
                }else{
                    binding.statusTv.text = "uploading token failed"
                    Log.d("NEW_TASK", "failure-- ${setDocTask.result}")
                }
            }
        }else{
            binding.statusTv.text = "getting token failed"
            Log.d("NEW_TASK", "failure-- ${tokenTask.result}")
        }
    }

}

private fun someMoreStuffs(user:User) {
    val body = Gson().toJson(user)

    val jsonObj = JSONObject()
    jsonObj.put("to",user.token)
    //jsonObj.put("notification", jsonNotifier)
    jsonObj.put("data", JSONObject(body))


    val request = okhttp3.Request.Builder()
        .url(FCM_URL)
        .addHeader("Content-Type", "application/json")
        .addHeader(
            "Authorization",
            WEB_KEY
        )
        .post(
            jsonObj.toString().toRequestBody(
                "application/json; charset=utf-8".toMediaType()
            )
        ).build()


    val logger = HttpLoggingInterceptor()
    logger.level = HttpLoggingInterceptor.Level.BASIC
    OkHttpClient.Builder().addInterceptor(logger)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build().newCall(request)
        .enqueue(object :
            Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("CLOUD_MSG", "onFailure-- $e")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(
                    "CLOUD_MSG", "onResponse-- $response +++ " +
                            "${call.isExecuted()}====${response.isSuccessful}" +
                            "--${response.code}===${response.body}"
                )
            }
        })

}

private fun getUsers(): Flow<List<User>> {
    binding.statusTv.text = "getting users...."
    return callbackFlow {
        binding.statusTv.text = "loading...."
        fireStoreRef.get().addOnCompleteListener { getUsers ->
            when {
                getUsers.isSuccessful -> {
                    val list = mutableListOf<User>()
                    if (!getUsers.result.isEmpty) {
                        list.clear()
                        for (docs in getUsers.result.documents) {
                            docs.toObject<User>()?.let {
                                if (it.token != deviceToken) {
                                    list.add(it)
                                }
                            }
                        }
                        Log.d("CLOUD_MSG", "SUCCESS--->$list")
                        binding.statusTv.text = "all users gotten"
                        trySend(list)
                        return@addOnCompleteListener
                    }
                    Log.d("CLOUD_MSG", "ERROR--->${getUsers.exception}")
                    binding.statusTv.text = "no user available"
                    trySend(emptyList())
                }
                else -> {
                    binding.statusTv.text = "unable to get users"
                    Log.d("CLOUD_MSG", "ERROR--->${getUsers.exception}")
                    trySend(emptyList())
                }
            }

        }

        awaitClose()
    }
}
fun lock() {
    CoroutineScope(Dispatchers.Main).launch{
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isScreenOn) {
            val policy = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            try {
                policy.lockNow()
            } catch (ex: SecurityException) {
                Toast.makeText(
                    this@MainActivity,
                    "You need to enable device administrator",
                    Toast.LENGTH_LONG
                ).show()
                val admin = ComponentName(this@MainActivity, SampleAdminReceiver::class.java)
                val intent: Intent = Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                ).putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin
                )
                startActivity(intent)
            }
        }
    }

}
 fun addAllSentSnapshot(currentUser: FirebaseUser) {
        fStoreReq.document(currentUser.uid).collection(SENT_REQUEST)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("me_allSentReqSnapshot", "Error---->> $error")
                    return@addSnapshotListener
                }
                Log.d("me_allSentReqSnapshot", "listener success---->> $error")
                loadAllSentReq(currentUser)
            }
    }
*/
