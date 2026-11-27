package com.dpjha.slidingnumbersplus.services

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dpjha.slidingnumbersplus.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlin.concurrent.thread

/**
 * Rewarded and between-level ads, behind the consent form. Debug builds use Google's test units.
 * A release build with no ad unit ids shows no ads and never starts the ad SDK.
 */
class Ads(private val activity: Activity) {
    private val enabled = BuildConfig.ADS_REWARDED.isNotBlank() || BuildConfig.ADS_INTERSTITIAL.isNotBlank()
    private var started = false
    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null

    var rewardedReady by mutableStateOf(false)
        private set

    fun start() {
        if (!enabled) return
        val consent = UserMessagingPlatform.getConsentInformation(activity)
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { if (consent.canRequestAds()) load() } },
            { if (consent.canRequestAds()) load() },
        )
        if (consent.canRequestAds()) load()
    }

    private fun load() {
        if (started) return
        started = true
        // starting the ad SDK is slow, so keep it off the main thread
        thread {
            MobileAds.initialize(activity.applicationContext)
            activity.runOnUiThread {
                loadRewarded()
                loadInterstitial()
            }
        }
    }

    private fun loadRewarded() {
        if (BuildConfig.ADS_REWARDED.isBlank() || rewarded != null) return
        RewardedAd.load(activity, BuildConfig.ADS_REWARDED, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewarded = ad
                rewardedReady = true
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedReady = false
            }
        })
    }

    private fun loadInterstitial() {
        if (BuildConfig.ADS_INTERSTITIAL.isBlank() || interstitial != null) return
        InterstitialAd.load(activity, BuildConfig.ADS_INTERSTITIAL, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitial = ad
            }
        })
    }

    fun showRewarded(onReward: () -> Unit) {
        val ad = rewarded ?: return
        rewarded = null
        rewardedReady = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = loadRewarded()
            override fun onAdFailedToShowFullScreenContent(error: AdError) = loadRewarded()
        }
        ad.show(activity) { onReward() }
    }

    /** Shows a between-level ad if one is ready, then continues. */
    fun showInterstitial(then: () -> Unit) {
        val ad = interstitial ?: run {
            if (started) loadInterstitial()
            return then()
        }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitial()
                then()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadInterstitial()
                then()
            }
        }
        ad.show(activity)
    }
}
