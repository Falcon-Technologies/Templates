package com.falcon.technologies.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build


class AndroidUtils
{
    companion object {
        fun isInternetConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
        // to check if we are connected to Network
        var isConnected = true

        // to check if we are monitoring Network
        private var monitoringConnectivity = false


        fun checkConnectivity(context: Context, connectivityCallback: ConnectivityManager.NetworkCallback): Boolean {
            // here we are getting the connectivity service from connectivity manager
            val connectivityManager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager?

            // Getting network Info
            // give Network Access Permission in Manifest
            val activeNetworkInfo = connectivityManager!!.activeNetworkInfo

            // isConnected is a boolean variable
            // here we check if network is connected or is getting connected
            isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
            if (!isConnected) {
                // SHOW ANY ACTION YOU WANT TO SHOW
                // WHEN WE ARE NOT CONNECTED TO INTERNET/NETWORK
//                LogUtility.LOGD(TAG, " NO NETWORK!")
                // if Network is not connected we will register a network callback to  monitor network
                connectivityManager.registerNetworkCallback(
                    NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(), connectivityCallback
                )
                monitoringConnectivity = true
                return false
            }
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(), connectivityCallback
            )

            return true
        }
    }
}