package com.falcon.technologies.adIntegration

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object InterstitialHelperClass {
    const val InterstitialTAG = "InterstitialADTag"
    private const val TAG = "AppLovinInterstitialADTag"
    private var interstitialTimeElapsed = 0L

    private var interstitialAdCallBack: InterstitialAdCallBack? = null
    private var mShowInterstitialListener: ShowInterstitialListener? = null
    var admobInterstitialAd: InterstitialAd? = null
    var fbInterstitialAd: com.facebook.ads.InterstitialAd? = null

    private var admobID = ""
    private var fbID = ""
    private var isAdLoading = false
    private const val CAPPING_TIME = 1
    private var timer: CountDownTimer? = null
    private var mActivity: Activity? = null
    var lovinInterstitialAd: MaxInterstitialAd? = null
    var isAdMobInterstitialFailed = false

    fun loadInterstitial(
        activity: Activity,
        condition: Int,
        admobId: String,
        fbId: String,
        appLovinId: String,
        interstitialAdCallBack: InterstitialAdCallBack
    ) {
        this.interstitialAdCallBack = interstitialAdCallBack
        when (condition) {

            (1) -> { //ADMOB ONLY
                loadAdmobInterstitial(activity, admobId, interstitialAdCallBack)
            }

            (2) -> { // FB ONLY
                loadFbInterstitial(activity, fbId, interstitialAdCallBack)
            }

            (3) -> { //App Lovin
                Log.i(TAG, "loadInterstitial: app lovin")
                loadAppLovinInterstitial(appLovinId, activity, interstitialAdCallBack)
            }
        }
    }


    private fun loadAdmobInterstitial(
        activity: Activity,
        adId: String?,
        interstitialAdCallBack: InterstitialAdCallBack
    ) {
        if (admobInterstitialAd == null) {
            isAdLoading = true
            Log.d(InterstitialTAG, "Ad load called.")
            if (adId != null) {
                admobID = adId
            }

            InterstitialAd.load(
                activity, admobID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(InterstitialTAG, adError.message)
                        admobInterstitialAd = null
                        isAdLoading = false
                        timer?.onFinish()
                        interstitialAdCallBack.onAdFailed(adError.toString())
                        isAdMobInterstitialFailed = true

                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(InterstitialTAG, "admob Ad was loaded.")
                        admobInterstitialAd = interstitialAd
                        isAdLoading = false
                        isAdMobInterstitialFailed = false
                        interstitialAdCallBack.onAdLoaded()
                    }
                })
        }
    }

    private fun loadFbInterstitial(
        activity: Activity,
        adId: String?,
        interstitialAdCallBack: InterstitialAdCallBack
    ) {
        val interstitialAdListener: InterstitialAdListener = object :
            InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                // Interstitial ad displayed callback
                Log.e(InterstitialTAG, "Interstitial ad displayed.")
                fbInterstitialAd = null
                mShowInterstitialListener?.onAdImpression()
            }

            override fun onInterstitialDismissed(ad: Ad) {
                // Interstitial dismissed callback
                Log.e(InterstitialTAG, "Interstitial ad dismissed.")
                mShowInterstitialListener?.onAdDismissed()
                isAdMobInterstitialFailed = false
            }

            override fun onError(ad: Ad, adError: AdError) {
                Log.e(InterstitialTAG, adError.toString())
                fbInterstitialAd = null
                isAdLoading = false
                timer?.onFinish()
                interstitialAdCallBack.onAdFailed(adError.toString())
            }

            override fun onAdLoaded(ad: Ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(InterstitialTAG, "Interstitial ad is loaded and ready to be displayed!")
                // Show the ad
                isAdLoading = false
                interstitialAdCallBack.onAdLoaded()
            }

            override fun onAdClicked(ad: Ad) {
                // Ad clicked callback
                Log.d(InterstitialTAG, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Ad impression logged callback
                Log.d(InterstitialTAG, "Interstitial ad impression logged!")
                interstitialTimeElapsed = Calendar.getInstance().timeInMillis
                fbInterstitialAd = null
            }
        }
        if (fbInterstitialAd == null) {
            isAdLoading = true
            Log.d(InterstitialTAG, "FB Ad load called.")
            if (adId != null) {
                fbID = adId
            }

            fbInterstitialAd = com.facebook.ads.InterstitialAd(activity, fbID)
            fbInterstitialAd?.loadAd(
                fbInterstitialAd?.buildLoadAdConfig()
                    ?.withAdListener(interstitialAdListener)
                    ?.build()
            )
        }
    }

    private fun loadAppLovinInterstitial(
        id: String,
        activity: Activity,
        interstitialAdCallBack: InterstitialAdCallBack
    ) {
        lovinInterstitialAd = MaxInterstitialAd(id, activity)

        lovinInterstitialAd?.setListener(object : MaxAdListener {

            override fun onAdLoaded(ad: MaxAd?) {
                Log.i(TAG, "onAdLoaded: ")
                interstitialAdCallBack.onAdLoaded()
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                Log.i(TAG, "onAdDisplayed: ")
                interstitialTimeElapsed = Calendar.getInstance().timeInMillis
            }

            override fun onAdClicked(ad: MaxAd?) {
                Log.i(TAG, "onAdClicked: ")
            }

            override fun onAdHidden(ad: MaxAd?) {
                // Interstitial ad is hidden. Pre-load the next ad
                mShowInterstitialListener?.onAdDismissed()
                Log.i(TAG, "onAdHidden: ")
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                // Interstitial ad failed to load
                // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)
                Log.i(TAG, error.toString())
                interstitialAdCallBack.onAdFailed(error.toString())
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                lovinInterstitialAd?.loadAd()
                Log.i(TAG, "onAdDisplayFailed: ")
            }
        })

        // Load the first ad
        lovinInterstitialAd?.loadAd()
    }


    /**
     * function for priority based loading ads
     */
    fun showAndLoadInterstitial(
        activity: Activity,
        condition: Int,
        showInterstitialListener: ShowInterstitialListener
    ) {
        mShowInterstitialListener = showInterstitialListener
        mActivity = activity
        when (condition) {
            (1) -> { //ADMOB ONLY
                //  LoadingDialog.showLoadingDialog(activity)
                showAdmobInterstitial(activity)
            }

            (2) -> { // FB ONLY
                //  LoadingDialog.showLoadingDialog(activity)
                showFbInterstitial(activity)
            }

            (3) -> {
                showAppLovinInterstitial()
            }

        }
    }


    private fun showAdmobInterstitial(activity: Activity) {
        if (isNetworkAvailable(activity)
            && timeDifference(interstitialTimeElapsed) > CAPPING_TIME
        ) {
            Log.i(InterstitialTAG, "showAndLoadInterstitial: admob show and load called")
            if (admobInterstitialAd != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    delay(300)
                    withContext(Dispatchers.Main) {
                        admobInterstitialAd?.show(activity)
                        admobInterstitialAd?.fullScreenContentCallback = object :
                            FullScreenContentCallback() {
                            override fun onAdImpression() {
                                super.onAdImpression()
                                Log.i(
                                    InterstitialTAG,
                                    "showAndLoadInterstitial: admob onAdImpression"
                                )
                                admobInterstitialAd = null
                                interstitialTimeElapsed = Calendar.getInstance().timeInMillis
                                mShowInterstitialListener?.onAdImpression()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                Log.i(
                                    InterstitialTAG,
                                    "showAndLoadInterstitial: admob onAdDismissedFullScreenContent"
                                )

                                mShowInterstitialListener?.onAdDismissed()
                            }
                        }

                    }
                }
            } else {

            }
        } else {

        }
    }

    private fun showFbInterstitial(activity: Activity) {
        if (isNetworkAvailable(activity) &&
            timeDifference(interstitialTimeElapsed) > CAPPING_TIME
        ) {
            Log.i(InterstitialTAG, "showAndLoadInterstitial: fb show and load called")
            if (fbInterstitialAd != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    delay(300)
                    withContext(Dispatchers.Main) {
                        if (fbInterstitialAd != null && fbInterstitialAd?.isAdLoaded == true) {
                            fbInterstitialAd?.show()

                        } else if (!isAdLoading) {

                        }
                    }
                }
            } else {

            }
        } else {

        }
    }

    private fun showAppLovinInterstitial() {
        Log.i(TAG, "showAppLovinInterstitial: ")
        if (lovinInterstitialAd?.isReady == true) {
            lovinInterstitialAd?.showAd()
            Handler(Looper.getMainLooper()).postDelayed({
                mShowInterstitialListener?.onAdImpression()
            }, 190)
        }
    }

    private fun timeDifference(millis: Long): Int {
        val current = Calendar.getInstance().timeInMillis
        val elapsedTime = current - millis
        return TimeUnit.MILLISECONDS.toSeconds(elapsedTime).toInt()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
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

    interface ShowInterstitialListener {
        fun onAdDismissed()
        fun onAdImpression()

    }

    interface InterstitialAdCallBack {
        fun onAdFailed(error: String)
        fun onAdLoaded()
    }
}