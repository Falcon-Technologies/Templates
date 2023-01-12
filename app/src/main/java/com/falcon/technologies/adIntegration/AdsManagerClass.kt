package com.falcon.technologies.adIntegration

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.facebook.ads.*
import com.falcon.technologies.R
import com.falcon.technologies.utils.AndroidUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class AdsManagerClass(
    private var mContext: Context?,
    private var mActivity: Activity?,
) {
    private lateinit var layoutInflater: LayoutInflater

    //admob variables
    private var mAdmobNativeId: String? = null

    //facebook variables
    private var mFbNativeId: String? = null
    private var mIsForSmall = false
    var fbNativeAd: NativeAd? = null
    var fbNativeBannerAd: NativeBannerAd? = null

    //app-lovin variables
    private lateinit var nativeAdLoader: MaxNativeAdLoader
    var lovingNativeAd: MaxAd? = null

    fun loadNativeAdWithPriority(
        priority: Int,
        frameLayout: FrameLayout?,
        adLoading: LinearLayout,
        admobNativeId: String,
        fbNativeId: String,
        lovinNativeId: String,
        isForSmall: Boolean,
        context: Context,
        nativeAdCallBack: NativeAdCallBack
    ) {

        if (frameLayout == null || priority == 0) {
            if (frameLayout != null) {
                frameLayout.removeAllViews()
                frameLayout.visibility = View.GONE
                adLoading.visibility = View.GONE
            }
            return
        }
        if (admobNativeId == "" && fbNativeId == "") {
            frameLayout.removeAllViews()
            frameLayout.visibility = View.GONE
            adLoading.visibility = View.GONE
            return
        }
        if (!AndroidUtils.isInternetConnected(context)) {
            frameLayout.visibility = View.GONE
            adLoading.visibility = View.GONE
            return
        } else {
            adLoading.visibility = View.GONE
        }

        mAdmobNativeId = admobNativeId
        mFbNativeId = fbNativeId
        mIsForSmall = isForSmall


        when (priority) {
            1 -> {
                loadAdmobNativeAd(frameLayout, adLoading, nativeAdCallBack)
            }

            2 -> {
                if (isForSmall) {
                    loadFbNativeBannerAd(frameLayout, adLoading, context, mActivity!!, fbNativeId)
                } else {
                    loadFbNativeAd(frameLayout, adLoading)
                }
            }

            3 -> {
                Log.i(TAG, "loadNativeAdWithPriorityWithLovin: ")
                loadAppLovinNativeAd(frameLayout, context, lovinNativeId)
            }

            else -> {
                frameLayout.visibility = View.GONE
                adLoading.visibility = View.GONE
            }
        }

    }

    @SuppressLint("InflateParams")
    private fun loadAdmobNativeAd(
        adFrame: FrameLayout,
        adLoading: LinearLayout,
        nativeAdCallBack: NativeAdCallBack
    ) {
        try {
            layoutInflater = LayoutInflater.from(mContext)
        } catch (ex: java.lang.Exception) {

            println("Google Native: medium " + ex.localizedMessage)
        }
        try {
            val builder = AdLoader.Builder(mContext!!, mAdmobNativeId!!)
            builder.forNativeAd { nativeAd ->
                try {
                    val adView: NativeAdView = if (mIsForSmall) {
                        println("Google Native: small")
                        layoutInflater.inflate(
                            R.layout.native_ad_small_layout, null
                        ) as NativeAdView
                    } else {
                        println("Google Native: medium")
                        layoutInflater.inflate(
                            R.layout.layout_native_full_item, null
                        ) as NativeAdView
                    }
                    adView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    populateNativeAdView(nativeAd, adView, mIsForSmall)
                    adFrame.removeAllViews()
                    adFrame.addView(adView)
                } catch (exception: Exception) {
                    println("Google Native: exception")
                    exception.printStackTrace()
                }
            }
            val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            builder.withNativeAdOptions(adOptions)
            val adLoader = builder.withAdListener(object : AdListener() {

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    nativeAdCallBack.onAdFailed()

                    adFrame.removeAllViews()
                    adFrame.visibility = View.GONE
                    adLoading.visibility = View.GONE
                    println("Google Native: failed to load")
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    nativeAdCallBack.onAdLoaded()
                    Log.i(TAG, "onAdLoaded:  ADMOB")
                    adFrame.visibility = View.VISIBLE
                    adLoading.visibility = View.GONE
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    println("Google Native: impression")/*  when(Constants.checkingAdLog){
                          6->{
                              Toast.makeText(context, "home native ad impression", Toast.LENGTH_SHORT).show()
                          }
                          7->{
                              Toast.makeText(context, "conversation native ad impression", Toast.LENGTH_SHORT).show()
                          }
                          8->{
                              Toast.makeText(context, "history native ad impression", Toast.LENGTH_SHORT).show()
                          }
                      }*/
                }
            }).build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (ignored: Exception) {
            println("Google Native: exception: " + ignored.localizedMessage)
        }

    }

    private fun loadFbNativeAd(frameLayout: FrameLayout, adLoading: LinearLayout) {

        layoutInflater = LayoutInflater.from(mContext)

        fbNativeAd = NativeAd(mContext, mFbNativeId)
        fbNativeAd!!.loadAd(
            fbNativeAd!!.buildLoadAdConfig().withAdListener(object : NativeAdListener {

                override fun onError(p0: Ad?, p1: AdError?) {
                    Log.e(TAG, "onError: ${p1?.errorMessage}")
                    frameLayout.visibility = View.GONE
                    adLoading.visibility = View.GONE
                }

                @SuppressLint("InflateParams")
                override fun onAdLoaded(ad: Ad) {
                    Log.d(TAG, "onAdLoaded")
                    if (fbNativeAd == null || fbNativeAd !== ad) {
                        println("Native AD fb native null")
                        return
                    }

                    println("Native AD fb native loaded")
                    val fbAdView: View =
                        layoutInflater.inflate(R.layout.fb_native_full_ad_layout, null)

                    inflateNativeAd(mContext, fbAdView, fbNativeAd!!)
                    frameLayout.removeAllViews()
                    frameLayout.addView(fbAdView)
                    frameLayout.visibility = View.VISIBLE
                    adLoading.visibility = View.GONE
                }

                override fun onAdClicked(ad: Ad) {
                    Log.d(TAG, "onAdClicked")
                    println("Native AD fb native clicked")
                }

                override fun onLoggingImpression(ad: Ad) {
                    Log.d(TAG, "onLoggingImpression ")
                    println("Native AD fb native impression")
                }

                override fun onMediaDownloaded(ad: Ad) {
                    Log.d(TAG, "onMediaDownloaded")
                    println("Native AD fb native media downloaded")
                }
            }).build()
        )
    }

    private fun loadFbNativeBannerAd(
        frameLayout: FrameLayout,
        adLoading: LinearLayout,
        context: Context,
        activity: Activity,
        fbNativeId: String
    ) {
        layoutInflater = LayoutInflater.from(mContext)
        fbNativeBannerAd = NativeBannerAd(context, fbNativeId)
        fbNativeBannerAd?.loadAd(
            fbNativeBannerAd?.buildLoadAdConfig()?.withAdListener(object : NativeAdListener {
                override fun onError(ad: Ad, adError: AdError) {
                    frameLayout.visibility = View.GONE
                    adLoading.visibility = View.GONE

                }

                override fun onAdLoaded(ad: Ad) {
                    if (fbNativeBannerAd == null || fbNativeBannerAd !== ad) {
                        return
                    }
                    val fbNativeBannerAdView: View =
                        layoutInflater.inflate(
                            R.layout.fb_native_banner_ad_layout,
                            null
                        )
                    inflateBannerAd(activity, fbNativeBannerAd!!, fbNativeBannerAdView)
                    frameLayout.removeAllViews()
                    frameLayout.addView(fbNativeBannerAdView)
                    frameLayout.visibility = View.VISIBLE
                    adLoading.visibility = View.GONE
                }

                override fun onAdClicked(ad: Ad) {}
                override fun onLoggingImpression(ad: Ad) {
                }

                override fun onMediaDownloaded(ad: Ad) {}
            })?.build()
        )
    }

    private fun loadAppLovinNativeAd(frameLayout: FrameLayout, context: Context, lovinNativeId: String) {

        nativeAdLoader = MaxNativeAdLoader(lovinNativeId, context)
        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {

            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (lovingNativeAd != null) {
                    nativeAdLoader.destroy(lovingNativeAd)
                }
                // Save ad for cleanup.
                lovingNativeAd = ad
                // Add ad view to view.
                frameLayout.removeAllViews()
                frameLayout.addView(nativeAdView)
                frameLayout.visibility = View.VISIBLE
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                super.onNativeAdLoadFailed(adUnitId, error)
                // We recommend retrying with exponentially higher delays up to a maximum delay
                Log.i(TAG, "onNativeAdLoadFailed: ")
                Log.e(TAG, "onNativeAdLoadFailed: ")
                frameLayout.removeAllViews()
                frameLayout.visibility = View.GONE
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                // Optional click callback
                Log.i(TAG, "onNativeAdClicked: ")
            }
        })
        nativeAdLoader.loadAd()
    }


    fun inflateNativeAd(mContext: Context?, fbAdView: View, nativeAd: NativeAd) {
        nativeAd.unregisterView()
        val nativeAdLayout: NativeAdLayout = fbAdView.findViewById(R.id.native_ad_container)
        val adChoicesContainer = fbAdView.findViewById<LinearLayout>(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(mContext, nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)
        val nativeAdIcon: MediaView = fbAdView.findViewById(R.id.native_ad_icon)
        val nativeAdTitle = fbAdView.findViewById<TextView>(R.id.native_ad_title)
        val nativeAdMedia: MediaView = fbAdView.findViewById(R.id.native_ad_media)
        val nativeAdSocialContext = fbAdView.findViewById<TextView>(R.id.native_ad_social_context)
        val nativeAdBody = fbAdView.findViewById<TextView>(R.id.native_ad_body)
        val sponsoredLabel = fbAdView.findViewById<TextView>(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction = fbAdView.findViewById<Button>(R.id.native_ad_call_to_action)
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility =
            if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)
        nativeAd.registerViewForInteraction(
            fbAdView, nativeAdMedia, nativeAdIcon, clickableViews
        )
    }

    fun inflateBannerAd(activity: Activity?, nativeAd: NativeBannerAd, adView: View) {
        nativeAd.unregisterView()
        val nativeAdLayout: NativeAdLayout = adView.findViewById(R.id.native_ad_container)
        // Add the AdOptionsView
        val adChoicesContainer = adView.findViewById<LinearLayout>(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(activity, nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView = adView.findViewById(R.id.native_ad_icon)
        val nativeAdTitle = adView.findViewById<TextView>(R.id.native_ad_title)
        val nativeAdSocialContext = adView.findViewById<TextView>(R.id.native_ad_social_context)
        val nativeAdBody = adView.findViewById<TextView>(R.id.native_ad_body)
        val sponsoredLabel = adView.findViewById<TextView>(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction: Button = adView.findViewById(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility =
            if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(adView, nativeAdIcon, clickableViews)
    }

    private fun populateNativeAdView(
        nativeAd: com.google.android.gms.ads.nativead.NativeAd,
        adView: NativeAdView,
        isForSmall: Boolean,
    ) {
        if (!isForSmall) {
            val mediaView: com.google.android.gms.ads.nativead.MediaView =
                adView.findViewById(R.id.ad_media)
            adView.mediaView = mediaView
        }
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.GONE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.GONE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }
        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.GONE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }
        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.GONE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.GONE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating?.toFloat() ?: 0F
            adView.starRatingView?.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.GONE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)

        println("Google Native: populated")

    }




    interface NativeAdCallBack {
        fun onAdLoaded()
        fun onAdFailed()
    }

    companion object {
        private const val TAG = "AdsManagerClassTags"
    }
}