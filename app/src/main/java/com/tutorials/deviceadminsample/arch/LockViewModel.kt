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
import com.google.gson.Gson
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver.Companion.updateToken
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver.Companion.updateTokenAdmin
import com.tutorials.deviceadminsample.model.RequestState
import com.tutorials.deviceadminsample.model.Resource
import com.tutorials.deviceadminsample.model.User
import com.tutorials.deviceadminsample.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val fStoreAdmin = Firebase.firestore.collection(USERS).document(ADMIN).collection("ALL")

    private val _signUpState = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val signUpState = _signUpState.asStateFlow()

    private val _loginState = MutableStateFlow<RequestState>(RequestState.NonExistent)
    val loginState = _loginState.asStateFlow()

    private val _allUsers = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val allUsers = _allUsers.asStateFlow()


    private val _userStatusEvent = Channel<UserEvents>()
    val userStatusEvent = _userStatusEvent.receiveAsFlow()

    sealed class UserEvents {
        object Successful : UserEvents()
        object Failure : UserEvents()
        object Error : UserEvents()
    }

    fun signUpNew(email: String, password: String) {
        _signUpState.value = RequestState.Loading
        viewModelScope.launch {
            try {
                val tokenReg = FirebaseMessaging.getInstance().token.await()
                val tokenList = listOf(tokenReg)
                val signUp = fAuth.createUserWithEmailAndPassword(email, password).await()
                val newUser = signUp.user?.uid?.let {
                    User(email = email, uid = it, deviceToken = tokenList, accountType = USER)
                } ?: User(email = email, deviceToken = tokenList, accountType = USER)
                fStoreUsers.document(email).set(newUser).await()
                _signUpState.value = RequestState.Successful(true)
                Log.d("me_addUsers", "SUCCESS ALl TRANSACTION COMPLETED")
            } catch (e: Exception) {
                _signUpState.value = RequestState.Failure("$e")
                Log.d("me_addUsers", "ERROR--->$e")
            }
        }

    }

    fun loginUser(context: Context, email: String, password: String) {
        _loginState.value = RequestState.Loading
        viewModelScope.launch {
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { login ->
                viewModelScope.launch {
                    try {
                        val tokenReg = FirebaseMessaging.getInstance().token.await()
                        updateToken(context, tokenReg)
                        if (login.isSuccessful) {
                            Log.d("me_login", "SUCCESS--->${login.isComplete}")
                            _loginState.value = RequestState.Successful(login.isComplete)
                        } else {
                            login.exception?.message?.let {
                                Log.d("me_login", "ERROR--->${login.exception}")
                                _loginState.value = RequestState.Failure(it)
                            } ?: RequestState.Failure("Error While Signing In...")

                        }
                    } catch (e: Exception) {
                        Log.d("me_login", "ERROR--->$e")
                    }
                }


            }

        }
    }

    fun getAllUsers() {
        viewModelScope.launch {
            fStoreUsers.get().addOnCompleteListener { getUsers ->
                when {
                    getUsers.isSuccessful -> {
                        val list = mutableListOf<User>()
                        if (!getUsers.result.isEmpty) {
                            list.clear()
                            for (docs in getUsers.result.documents) {
                                val user = docs.toObject<User>()
                                user?.let {
                                    if (it.email != fAuth.currentUser?.email) {
                                        list.add(it)
                                    }
                                }
                            }
                            Log.d("me_users", "SUCCESS--->$list")
                            _allUsers.value = Resource.Successful(list)
                            return@addOnCompleteListener
                        }
                        Log.d("me_users", "ERROR--->${getUsers.exception?.stackTrace}")
                        _allUsers.value = Resource.Failure("This App Currently Has No Users")
                    }
                    else -> {
                        Log.d("me_users", "ERROR--->${getUsers.exception?.localizedMessage}")
                        _allUsers.value = Resource.Failure("An error occurred, check your database if directory is available or network connectivity")
                    }
                }

            }
        }
    }

    fun signUpAdmin(email: String, password: String) {
        _signUpState.value = RequestState.Loading
        viewModelScope.launch {
            try {
                val tokenReg = FirebaseMessaging.getInstance().token.await()
                val tokenList = listOf(tokenReg)
                val signUp = fAuth.createUserWithEmailAndPassword(email, password).await()
                val newUser = signUp.user?.uid?.let {
                    User(email = email, uid = it, deviceToken = tokenList, accountType = ADMIN)
                } ?: User(email = email, deviceToken = tokenList, accountType = ADMIN)
                fStoreAdmin.document(email).set(newUser).await()
                _signUpState.value = RequestState.Successful(true)
                Log.d("me_addUsers", "SUCCESS ALl TRANSACTION COMPLETED")
            } catch (e: Exception) {
                _signUpState.value = RequestState.Failure("$e")
                Log.d("me_addUsers", "ERROR--->$e")
            }
        }

    }

    fun loginAdmin(context: Context, email: String, password: String) {
        _loginState.value = RequestState.Loading
        viewModelScope.launch {
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { login ->
                viewModelScope.launch {
                    try {
                        val tokenReg = FirebaseMessaging.getInstance().token.await()
                        updateTokenAdmin(context, tokenReg)
                        if (login.isSuccessful) {
                            Log.d("me_login", "SUCCESS--->${login.isComplete}")
                            _loginState.value = RequestState.Successful(login.isComplete)
                        } else {
                            login.exception?.message?.let {
                                Log.d("me_login", "ERROR--->${login.exception}")
                                _loginState.value = RequestState.Failure(it)
                            } ?: RequestState.Failure("Error While Signing In...")

                        }
                    } catch (e: Exception) {
                        Log.d("me_login", "ERROR--->$e")
                    }

                }

            }

        }
    }

    fun sendPushNotifier(user: User) {
        val body = Gson().toJson(user)

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
                        updateUserAlarmTime(user.email, user.alarmTime)
                    }

                    Log.d(
                        "CLOUD_MSG", "onResponse-- $response +++ " +
                                "${call.isExecuted()}====${response.isSuccessful}" +
                                "--${response.code}===${response.body}"
                    )
                }
            })

    }

    fun addAllUserSnapshot() {
        fStoreUsers.addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("me_allUserSnapshot", "Error---->> $error")
                return@addSnapshotListener
            }
            Log.d("me_allUserSnapshot", "listener success---->> $error")
            getAllUsers()
        }
    }

    fun addUserSnapshot(email: String) {
        fStoreUsers.addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("me_allUserSnapshot", "Error---->> $error")
                return@addSnapshotListener
            }
            Log.d("me_allUserSnapshot", "listener success---->> $error")
            checkIfUserExists(email)
        }
    }

    fun updateLogin() {
        _loginState.value = RequestState.NonExistent
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

  private fun checkIfUserExists(email: String) {
        fStoreUsers.document(email).get().addOnCompleteListener { getUser ->
            viewModelScope.launch {
                when {
                    getUser.isSuccessful -> {
                        if (!getUser.result.exists()) {
                            _userStatusEvent.send(UserEvents.Failure)
                            Log.d("me_users", "SUCCESS--->user account")
                            return@launch
                        }
                        _userStatusEvent.send(UserEvents.Successful)
                        Log.d("me_users", "ERROR--->user account ${getUser.exception}")
                    }
                    else -> {
                        _userStatusEvent.send(UserEvents.Error)
                        Log.d("me_users", "ERROR---> user account ${getUser.exception}")
                    }
                }

            }
        }
    }


}