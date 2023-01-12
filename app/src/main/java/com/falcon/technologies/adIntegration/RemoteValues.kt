package com.falcon.technologies.adIntegration

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.falcon.technologies.App
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object RemoteValues {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext())

    private const val INTER_MAIN_SCREEN = "inter_main_screen"
    private const val NATIVE_MAIN_SCREEN = "native_main_screen"
    private const val BANNER_MAIN_SCREEN = "banner_main_screen"

    var interMainScreen: Int
        get() = sharedPreferences.getInt(INTER_MAIN_SCREEN, 0)
        set(value) = sharedPreferences.edit {
            putInt(INTER_MAIN_SCREEN, value)
        }

    var nativeMainScreen: Int
        get() = sharedPreferences.getInt(NATIVE_MAIN_SCREEN, 0)
        set(value) = sharedPreferences.edit {
            putInt(NATIVE_MAIN_SCREEN, value)
        }

    var bannerMainScreen: Int
        get() = sharedPreferences.getInt(BANNER_MAIN_SCREEN, 0)
        set(value) = sharedPreferences.edit {
            putInt(BANNER_MAIN_SCREEN, value)
        }


    fun saveRemoteValues(remoteConfig: FirebaseRemoteConfig) {
        interMainScreen = remoteConfig.getLong(INTER_MAIN_SCREEN).toInt()
        nativeMainScreen = remoteConfig.getLong(NATIVE_MAIN_SCREEN).toInt()
        bannerMainScreen = remoteConfig.getLong(BANNER_MAIN_SCREEN).toInt()

    }

    fun assignRemoteValues() {
        with(AdsConstants) {
            inter_main_screen = interMainScreen
            nativeMainScreen = nativeMainScreen
            banner_main_screen = bannerMainScreen
        }
    }
}