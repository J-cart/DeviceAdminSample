package com.tutorials.deviceadminsample.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkObserver(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    fun observe(): Flow<NetworkStatus>{
        return callbackFlow {
            val callBack = object :ConnectivityManager.NetworkCallback(){
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    trySend(NetworkStatus.CONNECTED)
                }
                override fun onLost(network: Network) {
                    super.onLost(network)
                    trySend(NetworkStatus.DISCONNECTED)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    trySend(NetworkStatus.DISCONNECTED)
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callBack)
            awaitClose{
                connectivityManager.unregisterNetworkCallback(callBack)
            }
        }
    }
}