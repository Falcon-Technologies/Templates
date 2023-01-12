package com.falcon.technologies.adIntegration

import android.annotation.SuppressLint
import android.app.Activity
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.falcon.technologies.utils.logD
import com.falcon.technologies.utils.logE
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError

@SuppressLint("VisibleForTests")
object BannerAd {
    private var bannerAdView: AdView? = null
    var admobBannerAdView: com.google.android.gms.ads.AdView? = null


    fun loadBannerWithPriority(
        condition: Int,
        activity: Activity,
        adContainer: FrameLayout,
        admobBannerId: String,
        fbBannerId: String
    ) {

        when (condition) {
            0 -> {
                logD("Ad are Closed by publisher ")
                return
            }

            1 -> {
                loadAdMobBannerAd(activity, adContainer, admobBannerId)
            }

            2 -> {
                loadFbBannerAd(activity, adContainer, fbBannerId)
            }
        }

    }

    private fun loadAdMobBannerAd(activity: Activity, adLayout: FrameLayout?, admobBannerId: String) {
        admobBannerAdView = com.google.android.gms.ads.AdView(activity)
        admobBannerAdView?.adUnitId = admobBannerId
        admobBannerAdView?.setAdSize(getAdSize(activity))
        admobBannerAdView?.loadAd(AdRequest.Builder().build())
        admobBannerAdView?.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                adLayout?.removeAllViews()
                adLayout?.addView(admobBannerAdView)
                logD("Admob: ADMOB Banner loaded")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                logD("Admob: onAdClicked")
            }

            override fun onAdClosed() {
                super.onAdClosed()
                logD("Admob: onAdClosed")
            }

            override fun onAdOpened() {
                super.onAdOpened()
                logD("Admob: onAdOpened")
            }

            override fun onAdSwipeGestureClicked() {
                super.onAdSwipeGestureClicked()
                logD("Admob: onAdSwipeGestureClicked")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                adLayout?.removeAllViews()
                adLayout?.visibility = View.GONE
                logD("Admob onAdFailedToLoad")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                logD("Admob: onAdImpression")
            }
        }
    }

    fun loadFbBannerAd(activity: Activity, adContainer: FrameLayout, fbBannerId: String) {
        bannerAdView =
            AdView(activity, fbBannerId, AdSize.BANNER_HEIGHT_90)

        val fbBannerAdListener = object : AdListener {
            override fun onError(ad: Ad?, error: AdError?) {
                if (ad == bannerAdView) {
                    logE("Facebook: Banner failed to load: " + error?.errorMessage)
                }
            }

            override fun onAdLoaded(ad: Ad?) {
                if (ad == bannerAdView) {
                    logD("Facebook: onAdLoaded: ${ad?.placementId}")
                }
            }


            override fun onAdClicked(p0: Ad?) {
                logD("Facebook: onAdClicked: ${p0?.isAdInvalidated}")
            }

            override fun onLoggingImpression(p0: Ad?) {
                logD("Facebook: onLoggingImpression: ${p0?.javaClass}")
            }

        }
        bannerAdView?.let { nonNullBannerAdView ->
            adContainer.removeAllViews()
            adContainer.addView(nonNullBannerAdView)
            nonNullBannerAdView.loadAd(
                nonNullBannerAdView.buildLoadAdConfig().withAdListener(fbBannerAdListener).build()
            )
        }
    }


    private fun getAdSize(activity: Activity): com.google.android.gms.ads.AdSize {
        @Suppress("DEPRECATION") val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()

        return com.google.android.gms.ads.AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            activity,
            adWidth
        )
    }

}
