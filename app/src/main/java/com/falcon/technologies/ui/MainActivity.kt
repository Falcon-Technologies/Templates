package com.falcon.technologies.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.falcon.technologies.R
import com.falcon.technologies.adIntegration.AdsConstants
import com.falcon.technologies.adIntegration.AdsManagerClass
import com.falcon.technologies.databinding.ActivityMainBinding
import com.falcon.technologies.adIntegration.AudienceNetworkInitializeHelper
import com.falcon.technologies.adIntegration.BannerAd
import com.falcon.technologies.adIntegration.InterstitialHelperClass

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AudienceNetworkInitializeHelper.initialize(this)


        binding.refreshAdsButton.setOnClickListener {


            BannerAd.loadBannerWithPriority(
                AdsConstants.banner_main_screen,
                this@MainActivity,
                binding.bannerAdContainer,
                "",
                ""
            )
            loadInterstitialAd()

            loadNativeAd()
        }



        loadInterstitialAd()

        loadNativeAd()
    }


    private fun loadNativeAd() {
        AdsManagerClass(this, this).loadNativeAdWithPriority(2,
            binding.nativeAdContainer,
            binding.adLoader,
            "",
            getString(R.string.fb_native_main_screen),
            "",
            false,
            this,
            object : AdsManagerClass.NativeAdCallBack {
                override fun onAdLoaded() {}

                override fun onAdFailed() {}

            })
    }

    private fun loadInterstitialAd() {
        InterstitialHelperClass.loadInterstitial(
            this,
            2,
            "",
            getString(R.string.fb_inter_main_screen),
            "", object : InterstitialHelperClass.InterstitialAdCallBack {
                override fun onAdFailed(error: String) {

                }

                override fun onAdLoaded() {
                    InterstitialHelperClass.showAndLoadInterstitial(this@MainActivity,
                        2,
                        object : InterstitialHelperClass.ShowInterstitialListener {
                            override fun onAdDismissed() {}

                            override fun onAdImpression() {}

                        })
                }

            }
        )
    }
}
