package com.dpjha.slidingnumbersplus.services

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dpjha.slidingnumbersplus.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * The strip at the bottom of every screen. Its height is held from the start, so the board never
 * jumps under the player's finger when the ad arrives.
 */
@Composable
fun BannerAd(ready: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
        if (ready) {
            AndroidView(
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = BuildConfig.ADS_BANNER
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { it.destroy() },
            )
        }
    }
}
