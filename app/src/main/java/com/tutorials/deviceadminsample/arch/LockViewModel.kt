package com.tutorials.deviceadminsample.arch

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.tutorials.deviceadminsample.model.*
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver.Companion.updateDeviceToken
import com.tutorials.deviceadminsample.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LockViewModel : ViewModel() {

    private val fAuth = Firebase.auth
    private val fStoreUsers = Firebase.firestore.collection(USERS).document(USER).collection("ALL")
    private val fStoreAdmin = Firebase.firestore.collection(USERS).document(ADMIN).collection("ALL")

    private val _signUpState = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val signUpState = _signUpState.asStateFlow()

    private val _loginState = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val loginState = _loginState.asStateFlow()

    private val _allDevices = MutableStateFlow<Resource<List<DeviceInfo>>>(Resource.Loading())
    val allDevices = _allDevices.asStateFlow()

    private val _currentSelectedDevice = MutableStateFlow<Resource<DeviceInfo>>(Resource.Loading())
    val currentSelectedDevice = _currentSelectedDevice.asStateFlow()
    private val _currentUserDevice = MutableStateFlow<Resource<DeviceInfo>>(Resource.Loading())
    val currentUserDevice = _currentUserDevice.asStateFlow()

    sealed class UserEvents {
        object Successful : UserEvents()
        object Failure : UserEvents()
        object Error : UserEvents()
    }

    fun signUpNew(
        email: String,
        password: String,
        deviceId: String,
        deviceName: String,
        location: String
    ) {
        _signUpState.value = RequestState.Loading
        viewModelScope.launch {
            try {
                val tokenReg = FirebaseMessaging.getInstance().token.await()
                val tokenList = listOf(tokenReg)
                val signUp = fAuth.createUserWithEmailAndPassword(email, password).await()
                val newUser = signUp.user?.uid?.let {
                    User(email = email, uid = it, password = password)
                } ?: User(email = email, password = password)
                val device = DeviceInfo(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    deviceToken = tokenList,
                    location
                )
                fStoreUsers.document(email).set(newUser).await()
                fStoreUsers.document(email).collection(DEVICES).document(deviceId).set(device)
                    .await()
                _signUpState.value = RequestState.Successful(true)
                Log.d("me_addUsers", "SUCCESS ALl TRANSACTION COMPLETED")
            } catch (e: Exception) {
                _signUpState.value = RequestState.Failure("$e")
                Log.d("me_addUsers", "ERROR--->$e")
            }
        }

    }

    fun loginUser(
        context: Context,
        email: String,
        password: String,
        deviceId: String,
        deviceName: String,
        location: String
    ) {
        _loginState.value = RequestState.Loading
        viewModelScope.launch {
            try {
                val tokenReg = FirebaseMessaging.getInstance().token.await()
                val tokenList = listOf(tokenReg)
                fAuth.signInWithEmailAndPassword(email, password).await()
                val device = DeviceInfo(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    deviceToken = tokenList,
                    location
                )
                updateDeviceToken(context, tokenReg, device)
                _loginState.value = RequestState.Successful(true)
                Log.d("me_login", "SUCCESS ALl TRANSACTION COMPLETED")
            } catch (e: Exception) {
                _loginState.value = RequestState.Failure("$e")
                Log.d("me_login", "ERROR--->$e")
            }
        }

    }


    fun getAllUserDevice(email: String) {
        viewModelScope.launch {
            fStoreUsers.document(email).collection(DEVICES).get()
                .addOnCompleteListener { getDevices ->
                    when {
                        getDevices.isSuccessful -> {
                            val list = mutableListOf<DeviceInfo>()
                            if (!getDevices.result.isEmpty) {
                                list.clear()
                                for (docs in getDevices.result.documents) {
                                    val device = docs.toObject<DeviceInfo>()
                                    device?.let {
                                        list.add(it)
                                    }
                                }
                                Log.d("me_devices", "SUCCESS--->$list")
                                _allDevices.value = Resource.Successful(list)
                                return@addOnCompleteListener
                            }
                            Log.d("me_devices", "ERROR--->${getDevices.exception?.stackTrace}")
                            _allDevices.value = Resource.Failure("This App Currently Has No Users")
                        }
                        else -> {
                            Log.d(
                                "me_devices",
                                "ERROR--->${getDevices.exception?.localizedMessage}"
                            )
                            _allDevices.value =
                                Resource.Failure("An error occurred, check your database if directory is available or network connectivity")
                        }
                    }

                }
        }
    }


    fun sendPushNotifier(user: User) {
        /*   val body = Gson().toJson(user)

           val jsonObj = JSONObject()
           jsonObj.put("to", user.deviceToken.last())
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
                       if (user.commandType == ALARM) {
                           // TODO: update alarm data in firestore
                           updateUserAlarmTime(user.email, user.alarmTime)
                       }

                       Log.d(
                           "CLOUD_MSG", "onResponse-- $response +++ " +
                                   "${call.isExecuted()}====${response.isSuccessful}" +
                                   "--${response.code}===${response.body}"
                       )
                   }
               })*/

    }

    fun addAllDevicesSnapshot(email: String) {
        fStoreUsers.document(email).collection(DEVICES).addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("me_allDeviceSnapshot", "Error---->> $error")
                return@addSnapshotListener
            }
            getAllUserDevice(email)
            Log.d("me_allDeviceSnapshot", "listener success---->> $error")
        }
    }

    fun addDeviceSnapshot(email: String, deviceId: String) {
        fStoreUsers.document(email).collection(DEVICES).document(deviceId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("me_DevicesSnapshot", "Error---->> $error")
                    return@addSnapshotListener
                }
                Log.d("me_DevicesSnapshot", "listener success---->> $error")
               value?.toObject<DeviceInfo>()?.let{
                   _currentSelectedDevice.value = Resource.Successful(it)
               }?:return@addSnapshotListener
            }
    }

    fun resetOnSignOut() {
        _loginState.value = RequestState.NonExistent
        _currentUserDevice.value = Resource.Loading()
    }

    private fun updateUserAlarmTime(email: String, time: String) {
        viewModelScope.launch {
            try {
                fStoreUsers.document(email)
                    .update("alarmTime", time)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("me_updateAlarm", " ${it.result}")
                        } else {
                            Log.d("me_updateAlarm", " ${it.exception}")
                        }

                    }

                Log.d("me_updateAlarm", "SUCCESS")
            } catch (e: Exception) {
                Log.d("me_updateAlarm", "ERROR--->$e")
            }
        }
    }

    fun getCurrentUserInfo(email: String,deviceId: String){
        viewModelScope.launch {
            try {
                val userDevice = fStoreUsers.document(email).collection(DEVICES).document(deviceId).get().await()
                _currentUserDevice.value = Resource.Successful(userDevice.toObject<DeviceInfo>())
            }catch (e:Exception){
                _currentUserDevice.value = Resource.Failure(e.message)
            }
        }
    }

    fun updateUserLocation(email: String,location: String){
       viewModelScope.launch {
           if (_currentUserDevice.value is Resource.Successful){
               try {
                   _currentUserDevice.value.data?.let {
                       fStoreUsers.document(email).collection(DEVICES).document(it.deviceId)
                           .update("location", location).await()
                   }
               }catch (e:Exception){
                   Log.d("me_updateLocation", "updateUserLocation: some error occurred $e")
               }
               return@launch
           }
           Log.d("me_updateLocation", "updateUserLocation: some error occurred -- no current user data")
       }
    }
}