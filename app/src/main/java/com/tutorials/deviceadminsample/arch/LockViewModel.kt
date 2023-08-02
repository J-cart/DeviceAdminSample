package com.tutorials.deviceadminsample.arch

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.load
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.tutorials.deviceadminsample.model.*
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver.Companion.updateDeviceToken
import com.tutorials.deviceadminsample.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

class LockViewModel : ViewModel() {

    private val fAuth = Firebase.auth
    private val fStoreUsers = Firebase.firestore.collection(USERS).document(USER).collection("ALL")
    private val fStorage = FirebaseStorage.getInstance().reference.child(USERS)

    private val _signUpState = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val signUpState = _signUpState.asStateFlow()

    private val _loginState = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val loginState = _loginState.asStateFlow()

    private val _allDevices = MutableStateFlow<Resource<List<DeviceInfo>>>(Resource.Loading())
    val allDevices = _allDevices.asStateFlow()

    private val _currentSelectedDeviceInfo =
        MutableStateFlow<Resource<DeviceInfo>>(Resource.Loading())
    val currentSelectedDeviceInfo = _currentSelectedDeviceInfo.asStateFlow()

    private val _currentUserDeviceInfo = MutableStateFlow<Resource<DeviceInfo>>(Resource.Loading())
    val currentUserDeviceInfo = _currentUserDeviceInfo.asStateFlow()

    private val _currentUserInfo = MutableStateFlow<Resource<User>>(Resource.Loading())
    val currentUserInfo = _currentUserInfo.asStateFlow()

    private val _updateDetailsStatusFlow = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val updateDetailsStatusFlow = _updateDetailsStatusFlow.asStateFlow()

    private val _updateProfImgStatusFlow = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val updateProfImgStatusFlow = _updateProfImgStatusFlow.asStateFlow()

    private val _resetPasswordStatusFlow = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val resetPasswordStatusFlow = _resetPasswordStatusFlow.asStateFlow()

    fun signUpNew(
        email: String,
        password: String,
        userName: String,
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
                    User(email = email, uid = it, password = password, userName = userName)
                } ?: User(email = email, password = password, userName = userName)
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
                _signUpState.value = RequestState.Failure("${e.message}")
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
                _loginState.value = RequestState.Failure("${e.message}")
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


    fun sendPushNotifier(user: User, deviceInfo: DeviceInfo, remoteCommand: RemoteCommand) {

        if (deviceInfo.deviceToken.last() == "0") {
            Log.d("CLOUD_MSG", "ERROR-- device is offline")
            return
        }

        val body = Gson().toJson(remoteCommand)

        val jsonObj = JSONObject()
        jsonObj.put("to", deviceInfo.deviceToken.last())
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
                    if (response.isSuccessful || response.code == 200) {
                        addToDeviceCommandList(user.email, deviceInfo.deviceId, remoteCommand)
                    }

                }
            })

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
                    _currentSelectedDeviceInfo.value = Resource.Failure(error.message)
                    return@addSnapshotListener
                }
                Log.d("me_DevicesSnapshot", "listener success---->> $error")
                value?.toObject<DeviceInfo>()?.let {
                    _currentSelectedDeviceInfo.value = Resource.Successful(it)
                } ?: return@addSnapshotListener
            }
    }

    fun addDeviceUserInfoSnapshot(email: String) {
        fStoreUsers.document(email)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.d("me_DevicesUserInfoSnapshot", "Error---->> $error")
                    _currentSelectedDeviceInfo.value = Resource.Failure(error.message)
                    return@addSnapshotListener
                }
                Log.d("me_DevicesUserInfoSnapshot", "listener success---->> $error")
                value?.toObject<User>()?.let {
                    _currentUserInfo.value = Resource.Successful(it)
                } ?: return@addSnapshotListener
            }
    }

    fun resetOnSignOut() {
        _loginState.value = RequestState.NonExistent
        _currentUserDeviceInfo.value = Resource.Loading()
    }

    fun addToDeviceCommandList(email: String, deviceId: String, remoteCommand: RemoteCommand) {
        viewModelScope.launch {
            try {
                fStoreUsers.document(email).collection(DEVICES)
                    .document(deviceId).collection(COMMANDS)
                    .document(System.currentTimeMillis().toString()).set(remoteCommand).await()
                Log.d("me_updateCommand", "Successful...")
            } catch (e: Exception) {
                Log.d("me_updateCommand", "ERROR--->$e")
            }
        }

    }

    fun getCurrentUserDeviceInfo(email: String, deviceId: String) {
        viewModelScope.launch {
            try {
                val userDevice =
                    fStoreUsers.document(email).collection(DEVICES).document(deviceId).get().await()
                _currentUserDeviceInfo.value =
                    Resource.Successful(userDevice.toObject<DeviceInfo>())
            } catch (e: Exception) {
                _currentUserDeviceInfo.value = Resource.Failure(e.message)
            }
        }
    }

    fun getCurrentUserInfo(email: String) {
        viewModelScope.launch {
            try {
                val userInfo =
                    fStoreUsers.document(email).get().await()
                _currentUserInfo.value =
                    Resource.Successful(userInfo.toObject<User>())
            } catch (e: Exception) {
                _currentUserInfo.value = Resource.Failure(e.message)
            }
        }
    }

    fun updateUserLocation(email: String, location: String) {
        viewModelScope.launch {
            if (_currentUserDeviceInfo.value is Resource.Successful) {
                try {
                    _currentUserDeviceInfo.value.data?.let {
                        fStoreUsers.document(email).collection(DEVICES).document(it.deviceId)
                            .update("location", location).await()
                    }
                } catch (e: Exception) {
                    Log.d("me_updateLocation", "updateUserLocation: some error occurred $e")
                }
                return@launch
            }
            Log.d(
                "me_updateLocation",
                "updateUserLocation: some error occurred -- no current user data"
            )
        }
    }


    fun updateUserDetails(user: User) {
        _updateDetailsStatusFlow.value = RequestState.Loading
        viewModelScope.launch {
            try {
                fStoreUsers.document(user.email).set(user).await()
                _updateDetailsStatusFlow.value = RequestState.Successful(true)
            } catch (e: Exception) {
                Log.d("me_updateUserInfo", "updateUserInfo: some error occurred $e")
                _updateDetailsStatusFlow.value = RequestState.Failure(e.message.toString())
            }
            _updateDetailsStatusFlow.value = RequestState.NonExistent
            return@launch
        }
    }


    fun updateProfileImage(email: String, uri: Uri) {
        _updateProfImgStatusFlow.value = RequestState.Loading

        viewModelScope.launch {
            try {
                fStorage.child(email).putFile(uri).await()
                val url = fStorage.child(email).downloadUrl.await()
                fStoreUsers.document(email).update("imageUrl", url.toString()).await()
                _updateProfImgStatusFlow.value = RequestState.Successful(true)
            } catch (e: Exception) {
                _updateProfImgStatusFlow.value = RequestState.Failure(e.message.toString())
            }
            _updateProfImgStatusFlow.value = RequestState.NonExistent
        }

    }

    fun resetPassword(user: User, newPassword: String) {
        _resetPasswordStatusFlow.value = RequestState.Loading
        viewModelScope.launch {
            try {
                fAuth.currentUser?.let {fUser->
                    val auth =
                        fUser.reauthenticate(
                            EmailAuthProvider.getCredential(
                                user.email,
                                user.password
                            )
                        ).await()
                    fUser.updatePassword(newPassword).await()
                    fStoreUsers.document(user.email).update("password",newPassword).await()
                    _resetPasswordStatusFlow.value = RequestState.Successful(true)
                }


            } catch (e: Exception) {
                _resetPasswordStatusFlow.value = RequestState.Failure(e.message.toString())
            }

            _resetPasswordStatusFlow.value = RequestState.NonExistent

        }

    }

}