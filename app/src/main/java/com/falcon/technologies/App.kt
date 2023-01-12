package com.falcon.technologies

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.facebook.samples.ads.debugsettings.DebugSettings
import com.falcon.technologies.BuildConfig.DEBUG
import com.falcon.technologies.adIntegration.RemoteValues
import com.falcon.technologies.utils.showToast
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class App : Application() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        instance = this@App

        initialization()

    }

    private fun fetchRemoteConfigValues() {
        remoteConfig = Firebase.remoteConfig
        with(remoteConfig) {
            val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 20 }
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                    showToast("Fetch and activate succeeded")
                    RemoteValues.apply {
                        saveRemoteValues(remoteConfig)
                        assignRemoteValues()
                        Log.i(TAG, "fetchRemoteConfig: $interMainScreen")
                    }
                    Log.i(TAG, "fetchRemoteConfig complete: ${task.isSuccessful}")
                } else {
                    showToast("Fetch Failed")
                }
            }.addOnFailureListener {
                Log.i(TAG, "fetchRemoteConfigValues: ${it.cause} \n")
                Log.i(TAG, "fetchRemoteConfig: Failure ${it.message}")
            }

        }
    }

    private fun initialization() {

        //firebase initialization
        initializeFirebase(getContext())

        //Facebook Ads initialization
        AudienceNetworkAds.initialize(getContext())

        //fetch remote values from firebase remote configuration and save in shared-preference.
        fetchRemoteConfigValues()

        DebugSettings.initialize(getContext())

        //Add test device for ad-testing
        AdSettings.setTestMode(true)
        AdSettings.addTestDevice("add-device-id-from-logcat-here")

        strictPolicies()


    }

    /*
    * strict policies for checking network calls and memory leaks. It enables
    * developers to optimize their app so the user interface doesn't freeze.
    * */
    private fun strictPolicies() {
        if (DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
        }
    }

    private fun initializeFirebase(context: Context) {
        try {
            FirebaseApp.initializeApp(context)
        } catch (e: Exception) {
            Log.i(TAG, "onCreate: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "App"
        private var instance: App? = null
        fun getContext(): App {
            return instance!!
        }
    }

}